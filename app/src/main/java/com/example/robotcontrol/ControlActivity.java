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
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SocketServer.Socket;
import com.example.ImgDispose.ImgDispose;

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

//
//class Socket_Thread implements Runnable{
//    private Lock socketlock = new ReentrantLock();
//    public void run(){
//        Socket socketclient = new Socket();
//        try {
//            socketclient.Socket_init(MainActivity.IP,MainActivity.Part);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        byte[] data = {ControlActivity.Butn,ControlActivity.Light,ControlActivity.Speed};
//        ControlActivity.Butn = 0;
//        while (true){
//            data = new byte[]{ControlActivity.Butn,ControlActivity.Light,ControlActivity.Speed};
//            try {
//                ControlActivity.robot_data =   socketclient.Socket_s_r(data);
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}

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
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {





                System.out.println("设置图片");
                mimg_main.setImageBitmap((Bitmap)msg.obj);
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
        //检测滑块的变化 light亮度 并显示对应值
        msbr_light = findViewById(R.id.progress_light);
        mtex_light = findViewById(R.id.text_light);
        mimg_main = findViewById(R.id.img_main);
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
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
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
        Intent intent =getIntent();
        String ip =  intent.getStringExtra("robotIP");
        int part =  intent.getIntExtra("robotPart",8080);
        System.out.println("IP:"+ip+";Part:"+part);
        //用于接收数据用端口
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
                byte[] UdpBuffer = new byte[1050];
                int PackageSize=1024;  //数据包大小
                int PackageLen=0;
                int Packageid=0;   //数据包序号
                int Packageidnow=0;
                int PackageCount = 0; //数据包数量
                int frameSize=0;   //图像大小
                int frameSizenow = 0;
                int frameSizeOk=0;
                int frameid;    //帧id
                int frameidnow=0;
                byte[] FrameBuffer= new byte[10240];
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                do {
                    try {
                        UdpBuffer = udpsocket.RecData(1050);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                    if(UdpBuffer[10]<0){
                        continue;
                    }
                    frameid =  (int)(((UdpBuffer[1]&0xff) << 24) + (UdpBuffer[2]&0xff) << 16) + ((UdpBuffer[3]&0xff) << 8) + (UdpBuffer[4]&0xff); //获取帧号
                    frameSize = (int)(((UdpBuffer[5]&0xff) << 24) + ((UdpBuffer[6]&0xff) << 16) + ((UdpBuffer[7]&0xff) << 8) + (UdpBuffer[8]&0xff)); //获取帧大小
                    Packageid = (int)UdpBuffer[10]&0xff; //获取包号
                    PackageSize = (int)((UdpBuffer[13]&0xff)<< 8)|(UdpBuffer[14]&0xff);//获取包体积
                    if (frameidnow != frameid) { //换帧，记录新一帧的数据信息
                        frameidnow = frameid;    //更新帧号
                        frameSizenow = frameSize;  //更新帧体积
                        PackageCount = UdpBuffer[9]&0xff;   //更新数据包数量
                        PackageLen = ((UdpBuffer[11]&0xff) << 8)|(UdpBuffer[12]&0xff);  //更新数据包长度
                        System.out.println("framesize:"+frameSize+";frameID:"+frameid+";PackageCount:"+PackageCount);
                        //TODO 2020-11-30 10:02:57
                        frameSizeOk = 0 ;  //清除当前帧已接收数据量
                        Packageidnow = 0;    //最新已接收数据包号清零
                        os.reset();
                    }
//                    System.out.println("=======================================");
//                    System.out.println("Packageid:"+Packageid);
//                    System.out.println("PackageSize:"+PackageSize);
//                    System.out.println("PackageLen:"+PackageLen);
//                    System.out.println("=======================================");
                    if((Packageid<=PackageCount)&&(Packageid>Packageidnow)){
                        System.out.println("length:"+UdpBuffer.length);
                        if(PackageSize<=(UdpBuffer.length-15)){
                            if(PackageSize==PackageLen||Packageid==PackageCount){
                                Packageidnow = Packageid;
                                try {
                                    os.write(udpsocket.subBytes(UdpBuffer,15,PackageSize));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                frameSizeOk+=PackageSize;
                            }
                        }
                    }
                    if(frameSizeOk==frameSizenow){
                        byte[] byteArray = os.toByteArray();  //读取图像帧
                        System.out.println("picture:"+byteArray.length);
                        ImgDispose img = new ImgDispose();
                        Message msg =new Message();
                        msg.obj = img.getPicFromBytes(byteArray,null);//可以是基本类型，可以是对象，可以是List、map等
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                }while (true);//提示
            }
        }.start();
//        //=============================================================
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




