package com.pantherman594.mmsnet.server;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Listen or stop listening for MMS according to switch.
        final TextView status = (TextView) findViewById(R.id.ListenStatus);
        final Switch listenSwitch = (Switch) findViewById(R.id.ListenSwitch);

        // Check if switch is on/off, then change status text.
        listenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    status.setText(Server_Status[1]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4b77ef")));
                    //actionBar.setBackgroundDrawable(colorDrawable);
                }
                else{
                    status.setText(Server_Status[0]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7e7e7e")));
                }
            }

        });
    }

    final String[] Server_Status = {"Not listening for MMS","Listening for MMS"};
}
