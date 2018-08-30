package me.arnoldwho.arnold.aimonitor;

import com.baidu.speech.VoiceRecognitionService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

public class MySpeechRecognizer {

    public static final String TAG = MySpeechRecognizer.class.getSimpleName();
    private SpeechRecognizer speechRecognizer;
    @SuppressWarnings("unused")
    private Context context;
    private SpeechRecognizerCallBack callBack;
    public MySpeechRecognizer(Context context)
    {
        this.context = context;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context, new ComponentName(context, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(new MyRecognitionListener());
    }

    public void setCallBack(SpeechRecognizerCallBack callBack)
    {
        this.callBack = callBack;
    }

    public void Start()
    {
        Intent intent = new Intent();
        speechRecognizer.startListening(intent);
    }

    public void Start(Intent intent)
    {
        speechRecognizer.startListening(intent);
    }

    public void Stop()
    {
        speechRecognizer.stopListening();
    }

    public void cancel()
    {
        speechRecognizer.cancel();
    }

    public void Destroy()
    {
        speechRecognizer.destroy();
    }

    private class MyRecognitionListener implements RecognitionListener
    {
        @Override
        public void onBeginningOfSpeech() {

        }
        public void onRmsChanged(float rmsdB) {

        }
        public void onBufferReceived(byte[] buffer) {

        }
        public void onEndOfSpeech() {

        }
        public void onError(int error) {

        }
        public void onResults(Bundle results) {

            String text =  results.get("results_recognition").toString().replace("]", "").replace("[", "");
            callBack.getResult(text);

        }

        public void onPartialResults(Bundle partialResults) {

        }

        public void onEvent(int eventType, Bundle params) {

        }
        /**
         * 识别准备就绪，只有当此方法回调之后才能开始说话，否则会影响识别结果。
         */
        @Override
        public void onReadyForSpeech(Bundle params) {


        }
    }
    /**
     * 识别结果回调接口
     * @author Administrator
     *
     */
    public interface SpeechRecognizerCallBack
    {
        /**
         * 返回结果
         * @param result String 结果
         */
        public void getResult(String result);
    }
}

