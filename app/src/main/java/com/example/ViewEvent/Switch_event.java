package com.example.ViewEvent;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;

import com.example.robotcontrol.R;

public class Switch_event implements CompoundButton.OnCheckedChangeListener{
    public  static byte Switch_status=0;
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.nth_switch1:write_status(isChecked,0x01); break;
            case R.id.nth_switch2:write_status(isChecked,0x02); break;
            default: return ;
        }
    }
    private void write_status(boolean event, int flag){
        if(event){
            Log.d("Switch","=====Switch"+flag+":ON!====");
            Switch_status|=flag;
        }else{
            Log.d("Switch","=====Switch"+flag+":OFF!===");
            Switch_status&=(~flag);
        }
    }
}
