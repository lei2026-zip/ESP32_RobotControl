package com.example.robotcontrol;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SocketServer.Socket;
import com.example.ImgDispose.ImgDispose;
import com.example.ViewEvent.Onclick_Event;
import com.example.ViewEvent.SeekBar_listen;
import com.example.ViewEvent.Switch_event;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ControlActivity extends Activity {
    //seekbar
    protected SeekBar_listen seekbar = new SeekBar_listen();
    //图像显示区
    private ImageView mimg_main;

    protected static byte Control_flag = 0;
    protected static byte[] robot_data;
    private Lock btnlock = new ReentrantLock();
    //图像数据流
    public Handler mHandler_img = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                System.out.println("设置图片");

                mimg_main.setImageBitmap(ImgDispose.convert((Bitmap)msg.obj));
                System.out.println("msg : " + msg.obj);
            } else{
                String data = (String) msg.obj;
                Toast.makeText(ControlActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toast.makeText(ControlActivity.this,MainActivity.IP+":"+MainActivity.Part,Toast.LENGTH_SHORT).show();
        mimg_main = findViewById(R.id.img_main);
        //检测滑块的变化 light亮度 并显示对应值
        /**
         * 滑块初始化区
         */
        seekbar.setMsbr_light(findViewById(R.id.progress_light));
        seekbar.setMtex_light(findViewById(R.id.text_light));
        //检测滑块的变化 Speed速度 并显示对应值
        seekbar.setMsbr_speed(findViewById(R.id.progress_speed));
        seekbar.setMtex_speed(findViewById(R.id.text_speed));
        //初始化监听事件
        seekbar.init_allevent();
        //====================================================================
        /**
         * 按键初始化区
         */
        Onclick_Event Btnlisten = new Onclick_Event();
        ((Button)findViewById(R.id.Control_btn1)).setOnTouchListener(Btnlisten);
        ((Button)findViewById(R.id.Control_btn2)).setOnTouchListener(Btnlisten);
        ((Button)findViewById(R.id.Control_btn3)).setOnTouchListener(Btnlisten);
        ((Button)findViewById(R.id.Control_btn4)).setOnTouchListener(Btnlisten);
        //==================================================================
        Intent intent = getIntent();
        String ip =  intent.getStringExtra("robotIP");
        int part =  intent.getIntExtra("robotPart",8080);
        System.out.println("IP:"+ip+";Part:"+part);
        //=============================================================
        //====================================================================
        /**
         * switch 开关初始化区
         */
         Switch_event Switch_listen = new Switch_event();
        ((Switch)findViewById(R.id.nth_switch1)).setOnCheckedChangeListener(Switch_listen);
        ((Switch)findViewById(R.id.nth_switch2)).setOnCheckedChangeListener(Switch_listen);
        //===================================================================
        //===================================================================
        /**
         *  task 2
         *  开启 图像接收 协程
         *
         */
        //用于接收数据用端口
        //===================================================================
        //===================================================================
        new Thread(){
            public void run(){
                Socket udpsocket = new Socket();
                //获取目标iP
                InetAddress addr = null;
                try {
                    addr = InetAddress.getByName(ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                System.out.println("获取目标ip:"+addr);
                //此处初始化udpsocket ,并设置目标ip地址
                try {
                    udpsocket.SockInit(addr,part,1000);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                //
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int Receiver_Flag = 0;
                byte[] SendBuff = new byte[24];
                do {
                    try {
                        Receiver_Flag++;
                        if(udpsocket.UDPgetimage(os,mHandler_img,1050)){
                            System.out.println("wait_times:"+Receiver_Flag);
                        }
                        if(Receiver_Flag>30){
                            Receiver_Flag = 0;
                            SendBuff[0] = 0x55;
                            SendBuff[1] = Btnlisten.But_status;
                            SendBuff[2] = Switch_listen.Switch_status;
                            SendBuff[3] = SeekBar_listen.Light;
                            SendBuff[4] = SeekBar_listen.Speed;
                            udpsocket.SendData(SendBuff);
                        }
                    }catch (java.net.SocketTimeoutException timeout){
                        continue;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }while (true);//提示
            }
        }.start();
        //=========end====================================================
        //================================================================
    }


}




