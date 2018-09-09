package me.arnoldwho.arnold.aimonitor;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.mini.ActivityMiniRecog;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends ActivityMiniRecog {

    public Socket socket, videosocket;
    MySocket mySocket = new MySocket();
    final HardwareControl hardwareControl = new HardwareControl();
    protected boolean enableOffline = false;
    private boolean lightStatus = false;
    private boolean fanStatus = false;
    private boolean alarmStatus = false;
    private String recognizeResult = "";
    private String ip;
    private int port;
    Bitmap bitmap;


    private EventManager asr;

    @BindView(R.id.resultText)
    TextView _resultText;
    @BindView(R.id.et_mobile)
    TextInputEditText _mycmdText;
    @BindView(R.id.til_mobile)
    TextInputLayout tilMobile;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.startReco)
    Button _startReco;
    @BindView(R.id.lightSwitch)
    ImageView _lightSwitch;
    @BindView(R.id.fanSwitch)
    ImageView _fanSwitch;
    @BindView(R.id.alarmSwitch)
    ImageView _alarmSwitch;
    @BindView(R.id.video)
    ImageView _video;
    @BindView(R.id.hum)
    Button _humButton;
    @BindView(R.id.tem)
    Button _temButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _video.setImageResource(R.drawable.ic_temp);

        SharedPreferences pref = getSharedPreferences("serverinfo", MODE_PRIVATE);
        ip = pref.getString("serverip", ""  );
        port = Integer.parseInt(pref.getString("serverport", ""));
        initPermission();
        new Thread(playvideo).start();

        asr = EventManagerFactory.create(this, "asr");

        EventListener myListener = new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                if (name.equals("asr.partial")) {
                    if (params != null && !params.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(params);
                            if (jsonObject.optString("result_type").equals("final_result")) {
                                _resultText.setText(jsonObject.optString("best_result"));
                                recognizeResult = jsonObject.optString("best_result");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                switch (recognizeResult) {
                    case "开灯":
                        lightStatus = hardwareControl.lightOn(_lightSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "关灯":
                        lightStatus = hardwareControl.lightOff(_lightSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "开风扇":
                        fanStatus = hardwareControl.fanOn(_fanSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "关风扇":
                        fanStatus = hardwareControl.fanOff(_fanSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "开蜂鸣器":
                        alarmStatus = hardwareControl.alarmOn(_alarmSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "关蜂鸣器":
                        alarmStatus = hardwareControl.alarmOff(_alarmSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "关闭全部":
                        lightStatus = hardwareControl.lightOff(_lightSwitch, socket);
                        fanStatus = hardwareControl.fanOff(_fanSwitch, socket);
                        alarmStatus = hardwareControl.alarmOff(_alarmSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "打开全部":
                        lightStatus = hardwareControl.lightOn(_lightSwitch, socket);
                        fanStatus = hardwareControl.fanOn(_fanSwitch, socket);
                        alarmStatus = hardwareControl.alarmOn(_alarmSwitch, socket);
                        recognizeResult = "";
                        break;
                    case "现在温度多少":
                        hardwareControl.getTemInfo(_resultText, socket);
                    case "现在湿度多少":
                        hardwareControl.getHumInfo(_resultText, socket);
                }
            }
        };
        asr.registerListener(myListener);
        if (enableOffline) {
            loadOfflineEngine();
        }
        _startReco.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecognize();
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecognize();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    Runnable connect = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable videoconnect = new Runnable() {
        @Override
        public void run() {
            try {
                videosocket = new Socket(ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable playvideo = new Runnable() {
        @Override
        public void run() {
            try {
                Socket videosocket = new Socket(ip, port);
                while (true) {
                    showVideo(videosocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, ServerSActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void test() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!socket.isConnected()) {
                    new Thread(connect).start();
                }
                final String mycmd = _mycmdText.getText().toString();
                _resultText.setText(mySocket.getResponse(mycmd, socket));
            }
        }).start();
    }

    private void showVideo(Socket videosocket) {
        try {
                            String xi = "/getvideo";
                            OutputStream outputStream = videosocket.getOutputStream();
                            outputStream.write(xi.getBytes("utf-8"));
                            outputStream.flush();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(videosocket.getInputStream()));
                            String picLenString = bufferedReader.readLine();
                            int getPicLen = Integer.valueOf(picLenString);
                            outputStream = videosocket.getOutputStream();
                            String string = "1";
                            outputStream.write(string.getBytes());
                            outputStream.flush();
                            InputStream inputStream = videosocket.getInputStream();
                            int offset = 0;
                            byte[] bitmapBuff = new byte[getPicLen];
                            while(offset < getPicLen)
                            {
                                int len = inputStream.read(bitmapBuff, offset, getPicLen-offset);
                                offset+=len;
                            }
                            bitmap = BitmapFactory.decodeByteArray(bitmapBuff, 0, offset);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _video.setImageBitmap(bitmap);
                        }
                    });
    }

    private void startRecognize() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START;
        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 800);
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        params.put(SpeechConstant.PID, 1536);
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage();
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);
        String json = null;
        json = new JSONObject(params).toString();
        asr.send(event, json, null, 0, 0);
    }

    private void stopRecognize() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
    }

    private void loadOfflineEngine() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.DECODER, 2);
        params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
    }

    private void unloadOfflineEngine() {
        asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0);
    }


    @OnClick({R.id.fab, R.id.lightSwitch, R.id.fanSwitch, R.id.alarmSwitch, R.id.startReco, R.id.tem, R.id.hum})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                test();
                break;
            case R.id.lightSwitch:
                if (lightStatus) {
                    lightStatus = hardwareControl.lightOff(_lightSwitch, socket);
                } else {
                    lightStatus = hardwareControl.lightOn(_lightSwitch, socket);
                }
                break;
            case R.id.fanSwitch:
                if (fanStatus) {
                    fanStatus = hardwareControl.fanOff(_fanSwitch, socket);
                } else {
                    fanStatus = hardwareControl.fanOn(_fanSwitch, socket);
                }
                break;
            case R.id.alarmSwitch:
                if (alarmStatus) {
                    alarmStatus = hardwareControl.alarmOff(_alarmSwitch, socket);
                } else {
                    alarmStatus = hardwareControl.alarmOn(_alarmSwitch, socket);
                }
                break;
            case R.id.hum:
                    hardwareControl.getHumInfo(_resultText, socket);
                break;
            case R.id.tem:
                hardwareControl.getTemInfo(_resultText, socket);
                break;
                default:
                    break;


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        Log.i("ActivityMiniRecog", "On pause");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mySocket.getResponse("byebye.", socket);
                try {
                    socket.close();
                    videosocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(connect).start();
        new Thread(videoconnect).start();
        Toast.makeText(this, "reconnected socket!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        if (enableOffline) {
            unloadOfflineEngine();
        }
        asr.unregisterListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mySocket.getResponse("byebye.", socket);
                try {
                    socket.close();
                    videosocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }
}
