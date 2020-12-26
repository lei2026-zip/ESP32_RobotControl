package com.example.ViewEvent;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robotcontrol.ControlActivity;

public class SeekBar_listen {
    private SeekBar msbr_speed;   //seek 1
    private SeekBar msbr_light;   //seek 2
    private TextView mtex_speed;
    private TextView mtex_light;

    public static byte Speed;
    public static byte Light;

    public void init_allevent(){
        msbr_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Light = (byte) progress;
                mtex_light.setText("亮度:"+Integer.toString(progress)+"%");
                Log.d("SeekBar","=====Light:"+Light+"%=====");
            }
            @Override //开始滑动
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override //滑动结束
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        msbr_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
                Speed = (byte) progress;
                mtex_speed.setText("速度:"+Integer.toString(progress)+"%");
                Log.d("SeekBar","=====Speed:"+Speed+"%=====");
            }
            @Override //开始滑动
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override //滑动结束
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
            //开启soeket协程
        });
    }


    public SeekBar getMsbr_speed() {
        return msbr_speed;
    }

    public void setMsbr_speed(SeekBar msbr_speed) {
        this.msbr_speed = msbr_speed;
    }

    public SeekBar getMsbr_light() {
        return msbr_light;
    }

    public void setMsbr_light(SeekBar msbr_light) {
        this.msbr_light = msbr_light;
    }

    public TextView getMtex_speed() {
        return mtex_speed;
    }

    public void setMtex_speed(TextView mtex_speed) {
        this.mtex_speed = mtex_speed;
    }

    public TextView getMtex_light() {
        return mtex_light;
    }

    public void setMtex_light(TextView mtex_light) {
        this.mtex_light = mtex_light;
    }
}
