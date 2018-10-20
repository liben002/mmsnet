package com.pantherman594.mmsnet.client;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4b77ef"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // VPN on/off changes according to switch.
        final TextView status = (TextView) findViewById(R.id.VPN_Status);
        final Switch VPN_Switch = (Switch) findViewById(R.id.VPN_Switch);

        //ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.vpn_layout);

        // Checks for if the switch is on/off, then change the status text.
        VPN_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    status.setText(VPN_Status[1]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4b77ef")));
                    //actionBar.setBackgroundDrawable(colorDrawable);
                }
                else{
                    status.setText(VPN_Status[0]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7e7e7e")));
                }
            }

        });
    }

    final String[] VPN_Status = {"VPN: OFF","VPN: ON"};



   // VPN_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //    if (buttonView.isChecked()){
         //           status.setText(VPN_Status[1]);
         //       }
          //      else{
          //          status.setText(VPN_Status[0]);
         //       }
         //   }
       // });
}
