package com.example.robotcontrol;

import androidx.appcompat.app.AppCompatActivity;
import com.example.SocketServer.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SocketServer.Socket;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the PeripheralManager
 * For example, the snippet below will open a GPIO pin and set it to HIGH:
 * <p>
 * PeripheralManager manager = PeripheralManager.getInstance();
 * try {
 * Gpio gpio = manager.openGpio("BCM6");
 * gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * gpio.setValue(true);
 * } catch (IOException e) {
 * Log.e(TAG, "Unable to access GPIO");
 * }
 * <p>
 * You can find additional examples on GitHub: https://github.com/androidthings
 */
public class MainActivity extends Activity {
    private EditText mEditIP;
    private EditText mEditPa;
    private Button mBtnConnect;
    static int Part;
    static String IP;
    private int maxtries=3;  //设置最大重连次数
    private boolean thread_flag =false;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String data = (String)msg.obj;
            Toast.makeText(MainActivity.this,data,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          mBtnConnect =  (Button) findViewById(R.id.btn_connect);
          mBtnConnect.setOnClickListener(new View.OnClickListener() {
              @Override


              public void onClick(View v) {
                  if (thread_flag){
                      print_err("正在连接中请勿操作!");
                      return;
                  }
                  thread_flag = true;
                  Socket UdpSocket = new Socket();
                  mEditIP = findViewById(R.id.edit_ip);
                  mEditPa = findViewById(R.id.edit_part);
                  IP = mEditIP.getText().toString();
                  String p = mEditPa.getText().toString();
                  //初步检查输入的数据长度是否有效,无效则清空
                  if (IP == null || IP.length() > 15 || IP.length() < 11) {
                      mEditIP.setText("");
                      print_err("请输入正确IP地址!");
                      return;
                  } else if (p.length() == 0 || p.length() > 5) {
                      mEditPa.setText("");
                      print_err("请输入正确Part端口号!");
                      return;
                  }
                  Part = Integer.parseInt(p);
                  //=============================================================
                  //===============================================================
                  //用于接收数据用端口
                  new Thread(){
                      public void run(){
                          Socket udpsocket = new Socket();
                          //获取目标iP
                          InetAddress addr = null;
                          try {
                              addr = InetAddress.getByName(IP);
                          } catch (UnknownHostException e) {
                              e.printStackTrace();
                          }
                          System.out.println("获取目标ip:"+addr);
                          //此处初始化udpsocket ,并设置目标ip地址
                          try {
                              udpsocket.SockInit(addr,Part,2000);
                          } catch (SocketException e) {
                              e.printStackTrace();
                          }
                          //跳转到socket图传控制界面
                          try {
                              System.out.println("开始对接...");
                              //发送并监听本地ip端口
                              String sendbuff = "RobotControl V.1.0";
                              // 创建接收数据报，包含接收的buff
                              byte[] inbuff = new byte[512];
                              //提示
                              Message msg =new Message();
                              msg.obj = "连接中...";//可以是基本类型，可以是对象，可以是List、map等
                              mHandler.sendMessage(msg);
                              //
                              boolean res_response = false;  //判断是否接收数据
                              int tries = 0; //连接次数
                              do {
                                  udpsocket.SendData(sendbuff.getBytes());
                                  try {
                                      inbuff = udpsocket.RecData(512);
                                      if (!udpsocket.receivepacket.getAddress().equals(addr)) {
                                          System.out.println("目标ip:"+udpsocket.receivepacket.getAddress()+"设定ip:"+addr);
                                          throw new IOException("the packet from an unknow source");  //表示接收的的数据包来自一个未知的ip地址
                                      } else {
                                          System.out.println("对接成功!");
                                          String info = new String(inbuff,0,udpsocket.receivepacket.getLength());
                                          System.out.println(info);
                                          res_response = true;
                                      }
                                  }catch (java.net.SocketTimeoutException timeout){
                                      tries++;
                                      System.out.println("第" + tries + "次重试中..");
                                  }
                                  catch (IOException e) {
                                      Message msg2 =new Message();
                                      msg2.obj = "连接错误!";
                                      mHandler.sendMessage(msg2);
                                      return;
                                  }
                              } while ((!res_response) && (tries < maxtries));
                              if (res_response) {
                                  Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                                  intent.putExtra("robotIP", IP);
                                  intent.putExtra("robotPart", Part);
                                  startActivity(intent);
                              } else {
                                  System.out.println("连接超时");
                                  Message msg2 =new Message();
                                  msg2.obj = "连接超时!";//可以是基本类型，可以是对象，可以是List、map等
                                  mHandler.sendMessage(msg2);
                              }
                              udpsocket.socket.close();
                          } catch (SocketException e) {
                              System.out.println("创建连接失败");
                              Message msg2 =new Message();
                              msg2.obj = "创建连接失败!";//可以是基本类型，可以是对象，可以是List、map等
                              mHandler.sendMessage(msg2);
                              e.printStackTrace();
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                          thread_flag=false;
                          return;
                      }
                  }.start();
              }
          });
    }
    private void print_err(String err){
        Toast.makeText(MainActivity.this,err,Toast.LENGTH_SHORT).show();
    }
}

