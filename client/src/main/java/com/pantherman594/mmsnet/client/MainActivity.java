package com.pantherman594.mmsnet.client;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final String[] VPN_Status = {"VPN: OFF","VPN: ON"};
    boolean toastBoolean = true;
    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4b77ef"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // VPN on/off changes according to switch.
        final TextView status = (TextView) findViewById(R.id.VPN_Status);
        final Switch VPN_Switch = (Switch) findViewById(R.id.VPN_Switch);

        //ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.vpn_layout);
        final EditText editText = (EditText) findViewById(R.id.editText2);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                status.setText(VPN_Status[0]);
                VPN_Switch.setChecked(false);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7e7e7e")));
                editText.setEnabled(true);
                toastBoolean = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // Checks for if the switch is on/off, then change the status text.
        VPN_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Checks if there is a 10 digit number
                if (numAuthenticate(((EditText) findViewById(R.id.editText2)).getText().toString())) {
                    // If the switch is off, set status to OFF, color to grey, and make the field editable
                    if (!buttonView.isChecked()) {
                        status.setText(VPN_Status[0]);
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7e7e7e")));
                        editText.setEnabled(true);
                        toastBoolean = true;
                    } else {
                        status.setText(VPN_Status[1]);
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4b77ef")));
                        editText.setEnabled(false);
                    }
                    // If it's not on, keep off and editable
                } else {
                    VPN_Switch.setChecked(false);
                    status.setText(VPN_Status[0]);
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#7e7e7e")));
                    editText.setEnabled(true);
                    toastBoolean = true;
                }
            }
        });
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.editText2);
        String phoneNumber = editText.getText().toString();
        if (numAuthenticate(phoneNumber)) {
            ((Switch) findViewById(R.id.VPN_Switch)).setChecked(true);
        }
        System.out.println(phoneNumber);
    }

    public boolean numAuthenticate(String phoneNumber) {
        int duration = Toast.LENGTH_SHORT;
        if(phoneNumber == null || phoneNumber.length() != 10) {
            if (toastBoolean) {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Please submit a 10-digit phone number", duration);
                toast.show();
                return false;
            } else {
                toastBoolean = false;
            }
        }
        return true;
    }
}
