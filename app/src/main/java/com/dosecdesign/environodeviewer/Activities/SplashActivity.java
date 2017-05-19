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

public class SplashActivity extends AppCompatActivity {

    private ProgressDialog mProg;
    private HttpUtils mHttpUtil;
    private DeviceMemoryUtils mDeviceMem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);


        mHttpUtil = new HttpUtils();

        mDeviceMem = new DeviceMemoryUtils();

        String baseUrl = mHttpUtil.buildUrl();
        try {
            URL url = new URL(baseUrl);
            new RetrieveJsonDataTask().execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.url_unrecognised, Toast.LENGTH_SHORT).show();
        }


    }

    public class RetrieveJsonDataTask extends AsyncTask<URL, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        HttpURLConnection mConnection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(URL... params) {


            URL url = params[0];
            Log.d(Constants.DEBUG_TAG, "RetrieveJsonTask url :" + url);

            try {

                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.connect();

                InputStream stream = mConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                StringBuffer buffer = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null){

                // Server has returned a result. Save the result to cache.
                mDeviceMem.saveToCache(getCacheDir(),result,getBaseContext(), "site");

                // Close splash screen and start application

            }
            Intent startAppIntent = new Intent(SplashActivity.this, SearchActivity.class);
            //startAppIntent.putExtra(result, Constants.STRING_RESULT);
            startActivity(startAppIntent);
            finish();
        }
    }
}
