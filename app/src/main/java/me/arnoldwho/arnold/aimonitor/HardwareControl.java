package me.arnoldwho.arnold.aimonitor;

import android.widget.ImageView;
import android.widget.TextView;

import java.net.Socket;


public class HardwareControl {
    MySocket mySocket = new MySocket();
    String response;
    String temp;

    public boolean lightOn (ImageView imageView, final Socket socket) {
        imageView.setImageResource(R.drawable.ic_light_on);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/superpowerlighton", socket);
            }
        }).start();
        return true;
    }

    public boolean lightOff (ImageView imageView, final Socket socket) {
        imageView.setImageResource(R.drawable.ic_light_off);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/superpowerlightoff", socket);
            }
        }).start();
        return false;
    }

    public boolean fanOn (ImageView imageView, final Socket socket) {
        imageView.setImageResource(R.drawable.ic_fan_on);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/superpowerfanon", socket);
            }
        }).start();
        return true;
    }

    public boolean fanOff (ImageView imageView, final Socket socket) {
        imageView.setImageResource(R.drawable.ic_fan_off);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/superpowerfanoff", socket);
            }
        }).start();
        return false;
    }

    public boolean alarmOn (ImageView imageView, final Socket socket) {
        imageView.setImageResource(R.drawable.ic_alarm_on);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/superpoweralarmon", socket);
            }
        }).start();
        return true;
    }

    public boolean alarmOff (ImageView imageView, final Socket socket) {
        imageView.setImageResource(R.drawable.ic_alarm_off);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/superpoweralarmoff", socket);
            }
        }).start();
        return false;
    }

    public void getTemInfo (final TextView textView, final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tmp = mySocket.getResponse("/getsuperpowertem", socket);
                temp = "Tem = " + tmp;
                textView.setText(temp);
                temp = "";
                }
        }).start();
    }

    public void  getHumInfo (final TextView textView, final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tmp = mySocket.getResponse("/getsuperpowerhum", socket);
                temp = "Hum = " + tmp;
                textView.setText(temp);
                temp = "";
            }
        }).start();
    }
}
