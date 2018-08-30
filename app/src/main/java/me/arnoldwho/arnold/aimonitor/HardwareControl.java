package me.arnoldwho.arnold.aimonitor;

import android.widget.ImageView;

public class HardwareControl {

    public boolean lightOn (ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_light_on);
        return true;
    }

    public boolean lightOff (ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_light_off);
        return false;
    }

    public boolean fanOn (ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_fan_on);
        return true;
    }

    public boolean fanOff (ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_fan_off);
        return false;
    }

    public boolean alarmOn (ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_alarm_on);
        return true;
    }

    public boolean alarmOff (ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_alarm_off);
        return false;
    }


}
