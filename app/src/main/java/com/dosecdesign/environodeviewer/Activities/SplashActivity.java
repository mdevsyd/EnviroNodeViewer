package com.dosecdesign.environodeviewer.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.dosecdesign.environodeviewer.Utitilies.DeviceMemoryUtils;
import com.dosecdesign.environodeviewer.Utitilies.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Load screen activity that simulates program loading while
 * showing EnviroNode logo. Shows a progress bar during
 * simulated load time.
 */

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        sleepThread();
    }

    private void sleepThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                    Thread.sleep(1000);

                    startApplication();


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Starts the EnviroNode Viewer application
     */

    public void startApplication(){
        Intent startAppIntent = new Intent(SplashActivity.this, OptionSelectActivity.class);
        startActivity(startAppIntent);
        // finish this activity so the user can't return to splash screen ever
        finish();
    }

}
