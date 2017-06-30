package com.dosecdesign.environodeviewer.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dosecdesign.environodeviewer.R;

/**
 * Activity where the user can select to either connect to a remote Bluetooth device
 * or view historical server data.
 */

public class OptionSelectActivity extends AppCompatActivity {

    private Button mViewDataBtn;
    private Button mConnectDeviceBtn;

    /**
     * Setup both buttons and the onlclik listeners to take user to the applicable activity
     * based on their seleection
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_select);

        mViewDataBtn = (Button) findViewById(R.id.viewDatBtn);
        mConnectDeviceBtn = (Button) findViewById(R.id.connectBtn);

        // If view data is clicked, send user to site search activity
        mViewDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showCustomDialog();
                Intent apiIntent = new Intent(OptionSelectActivity.this, SearchActivity.class);;
                startActivity(apiIntent);
            }
        });

        // If Connect is clicked, open device list activity
        mConnectDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent connectBTDeviceIntent = new Intent(OptionSelectActivity.this, DeviceListActivity.class);
                startActivity(connectBTDeviceIntent);
            }
        });
    }


}
