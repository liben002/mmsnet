package com.pantherman594.mmsnet.server;

import android.Manifest;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("AAAAAAAA", "BEGIN");

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.VIBRATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_SETTINGS
        }, 1);

        // Listen or stop listening for MMS according to switch.
        final TextView status = (TextView) findViewById(R.id.ListenStatus);
        final Switch listenSwitch = (Switch) findViewById(R.id.ListenSwitch);

        final ClientTask clientTask = ServerApplication.getInstance().getClientTask();

        // Check if switch is on/off, then change status text.
        listenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    status.setText(Server_Status[1]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#54d160")));
                    //actionBar.setBackgroundDrawable(colorDrawable);
                    Log.i("AAAAAAAA", "start");
                    clientTask.execute(getContentResolver());
                } else {
                    status.setText(Server_Status[0]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7e7e7e")));
                    Log.i("AAAAAAAA", "end");
                    clientTask.cancel(false);
                    Log.i("AAAAAAAA", "end after");
                }
            }

        });
    }

    final String[] Server_Status = {"Not listening for MMS","Listening for MMS"};
}
