package com.dosecdesign.environodeviewer.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dosecdesign.environodeviewer.R;

public class SplashActivity extends AppCompatActivity {

    private ProgressDialog mProg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mProg = new ProgressDialog(this);
        mProg.setIndeterminate(true);
        mProg.setCancelable(false);
        mProg.setMessage("Loading...");
        mProg.show();

        Thread splash = new Thread(){

            @Override
            public void run() {

                try {
                    super.run();
                    sleep(2000);  //Delay of 2 seconds
                } catch (Exception e) {

                } finally {

                    Intent i = new Intent(SplashActivity.this, SearchActivity.class);

                    startActivity(i);
                    finish();
                }
            }
        };
        mProg.dismiss();
        splash.start();




    }
}
