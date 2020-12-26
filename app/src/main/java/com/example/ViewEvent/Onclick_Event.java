package com.example.ViewEvent;


import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.robotcontrol.R;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Onclick_Event implements View.OnTouchListener{
    public static byte But_status;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()){
                case R.id.Control_btn1: write_status(event,0x01); break;
                case R.id.Control_btn2: write_status(event,0x02); break;
                case R.id.Control_btn3: write_status(event,0x04); break;
                case R.id.Control_btn4: write_status(event,0x08); break;
                default: return false;
            }
            return true;
        }

        private void write_status(MotionEvent event,int flag){
            if(event.getAction() == MotionEvent.ACTION_UP){
                Log.d("Button","=====Button"+flag+":Up!=====");
                But_status&=(~flag);
            }
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                Log.d("Button","=====Button"+flag+":Down!===");
                But_status|=flag;
            }
        }
    }
