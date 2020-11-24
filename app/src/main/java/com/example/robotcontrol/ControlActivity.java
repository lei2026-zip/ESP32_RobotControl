package com.example.robotcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SocketServer.Socket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class Socket_Thread implements Runnable{
    private Lock socketlock = new ReentrantLock();
    public void run(){
        Socket socketclient = new Socket();
        socketclient.Socket_init(MainActivity.IP,MainActivity.Part);
        byte[] data = {ControlActivity.Butn,ControlActivity.Light,ControlActivity.Speed};
        while (true){
            data = new byte[]{ControlActivity.Butn,ControlActivity.Light,ControlActivity.Speed};
            socketclient.Socket_send(data);
            ControlActivity.robot_data = socketclient.Socket_rec();
        }
    }
}

public class ControlActivity extends Activity {
    private ImageView mimg_main;
    private SeekBar msbr_speed;
    private SeekBar msbr_light;
    private TextView mtex_speed;
    private TextView mtex_light;
    protected static byte Speed;
    protected static byte Light;
    protected static byte Butn;
    protected static byte[] robot_data;
    protected static byte[] picture_buff;
    private Lock btnlock = new ReentrantLock();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toast.makeText(ControlActivity.this,MainActivity.IP+":"+MainActivity.Part,Toast.LENGTH_SHORT).show();
        mimg_main = findViewById(R.id.img_main);
        //检测滑块的变化 light亮度 并显示对应值
        msbr_light = findViewById(R.id.progress_light);
        mtex_light = findViewById(R.id.text_light);
        msbr_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               Light = (byte) progress;
               mtex_light.setText("亮度:"+Integer.toString(progress)+"%");
            }
            @Override //开始滑动
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override //滑动结束
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //检测滑块的变化 Speed速度 并显示对应值
        msbr_speed = findViewById(R.id.progress_speed);
        mtex_speed = findViewById(R.id.text_speed);
        msbr_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Speed = (byte) progress;
                mtex_speed.setText("速度:"+Integer.toString(progress)+"%");
            }
            @Override //开始滑动
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override //滑动结束
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            //开启soeket协程
        });
        Socket_Thread test1 = new Socket_Thread();
        Thread thread = new Thread(test1);
        thread.start();
        //=============================================================
        while(true){
            if(ControlActivity.robot_data!=null){
                Toast.makeText(ControlActivity.this,ControlActivity.robot_data.toString(),Toast.LENGTH_SHORT).show();;
            }
        }
    }

   //检测按钮点击事件  并保存
    public void on_click_top(View view){
        btnlock.lock();
        try{
            Butn|=8;
        }finally{
            btnlock.unlock();
        }
    }
    public void on_click_left(View view){
        btnlock.lock();
        try{
            Butn|=4;
        }finally{
            btnlock.unlock();
        }
    }
    public void on_click_right(View view){
        btnlock.lock();
        try{
            Butn|=2;
        }finally{
            btnlock.unlock();
        }
    }
    public void on_click_bottom(View view){
        btnlock.lock();
        try{
            Butn|=1;
        }finally{
            btnlock.unlock();
        }
    }
}

