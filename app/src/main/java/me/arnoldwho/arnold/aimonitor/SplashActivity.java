package me.arnoldwho.arnold.aimonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        super.onCreate(savedInstanceState);
        SystemClock.sleep(1500);
        firstStart();
        finish();
    }

    public void firstStart(){
        SharedPreferences pref = getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = pref.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = getSharedPreferences("share", MODE_PRIVATE).edit();
        if (isFirstRun)
        {
            editor.putBoolean("isFirstRun", false);
            editor.apply();
            startActivity(new Intent(SplashActivity.this, ServerSActivity.class));
        } else
        {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
    }

}
