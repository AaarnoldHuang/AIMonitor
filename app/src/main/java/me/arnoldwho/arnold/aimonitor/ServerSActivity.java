package me.arnoldwho.arnold.aimonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ServerSActivity extends AppCompatActivity {

    @BindView(R.id.input_server)
    EditText _serverText;
    @BindView(R.id.input_port)
    EditText _serverPort;
    @BindView(R.id.btn_ok)
    Button _okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_s);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void  login() {
        SharedPreferences.Editor editor = getSharedPreferences("serverinfo",MODE_PRIVATE).edit();
        editor.putString("serverip", _serverText.getText().toString());
        editor.putString("serverport", _serverPort.getText().toString());
        editor.apply();
        startActivity(new Intent(ServerSActivity.this, MainActivity.class));
        finish();

    }

}
