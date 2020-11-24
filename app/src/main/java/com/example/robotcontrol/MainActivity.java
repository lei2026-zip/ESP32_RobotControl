package com.example.robotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SocketServer.Socket;

import java.net.InetAddress;

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
    static InetAddress IP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          mBtnConnect =  (Button) findViewById(R.id.btn_connect);
          mBtnConnect.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Socket UdpSocket = new Socket();
                  mEditIP =   findViewById(R.id.edit_ip);
                  mEditPa =   findViewById(R.id.edit_part);
                  IP = (InetAddress) mEditIP.getText();
                  Part =Integer.parseInt(mEditPa.getText().toString());
                  //初步检查输入的数据长度是否有效,无效则清空
                  if(Part==0 ||Part>9999){
                      print_err("请输入正确Part端口号!",mEditPa);
                      return;
                  }
                  //跳转到socket图传控制界面
                  Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                  startActivity(intent);
              }
          });
    }

    private void print_err(String err,EditText e){
        e.setText("");
        Toast.makeText(MainActivity.this,err,Toast.LENGTH_SHORT).show();;
    }
}
