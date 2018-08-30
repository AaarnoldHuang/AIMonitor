package me.arnoldwho.arnold.aimonitor;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends ActivityMiniRecog {

    public Socket socket;
    MySocket mySocket = new MySocket();
    final HardwareControl hardwareControl = new HardwareControl();
    protected boolean enableOffline = false;
    private boolean lightStatus = false;
    private boolean fanStatus = false;
    private boolean alarmStatus = false;
    private String recognizeResult = "";
    private String ip;
    private int port;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences pref = getSharedPreferences("serverinfo", MODE_PRIVATE);
        ip = pref.getString("serverip", "");
        port = Integer.parseInt(pref.getString("serverport", ""));
        initPermission();
        new Thread(connect).start();
        asr = EventManagerFactory.create(this, "asr");
        EventListener myListener = new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
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
                switch (recognizeResult) {
                    case "开灯":
                        lightStatus = hardwareControl.lightOn(_lightSwitch);
                        break;
                    case "关灯":
                        lightStatus = hardwareControl.lightOff(_lightSwitch);
                        break;
                    case "开风扇":
                        fanStatus = hardwareControl.fanOn(_fanSwitch);
                        break;
                    case "关风扇":
                        fanStatus = hardwareControl.fanOff(_fanSwitch);
                        break;
                    case "开蜂鸣器":
                        alarmStatus = hardwareControl.alarmOn(_alarmSwitch);
                        break;
                    case "关蜂鸣器":
                        alarmStatus = hardwareControl.alarmOff(_alarmSwitch);
                        break;
                    case "关闭全部":
                        lightStatus = hardwareControl.lightOff(_lightSwitch);
                        fanStatus = hardwareControl.fanOff(_fanSwitch);
                        alarmStatus = hardwareControl.alarmOff(_alarmSwitch);
                        break;
                    case "开启全部":
                        lightStatus = hardwareControl.lightOn(_lightSwitch);
                        fanStatus = hardwareControl.fanOn(_fanSwitch);
                        alarmStatus = hardwareControl.alarmOn(_alarmSwitch);
                        break;
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
                        testT();
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

    private void testT () {
        Toast.makeText(this, "haha", Toast.LENGTH_LONG).show();
    }

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


    @OnClick({R.id.fab, R.id.startReco, R.id.lightSwitch, R.id.fanSwitch, R.id.alarmSwitch})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                test();
                break;
            case R.id.startReco:
                break;
            case R.id.lightSwitch:
                if (lightStatus) {
                    lightStatus = hardwareControl.lightOff(_lightSwitch);
                } else {
                    lightStatus = hardwareControl.lightOn(_lightSwitch);
                }
                break;
            case R.id.fanSwitch:
                if (fanStatus) {
                    fanStatus = hardwareControl.fanOff(_fanSwitch);
                } else {
                    fanStatus = hardwareControl.fanOn(_fanSwitch);
                }
                break;
            case R.id.alarmSwitch:
                if (alarmStatus) {
                    alarmStatus = hardwareControl.alarmOff(_alarmSwitch);
                } else {
                    alarmStatus = hardwareControl.alarmOn(_alarmSwitch);
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        Log.i("ActivityMiniRecog", "On pause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        if (enableOffline) {
            unloadOfflineEngine();
        }
        asr.unregisterListener(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
