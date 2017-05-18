package com.dosecdesign.environodeviewer.Utitilies;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Michi on 18/05/2017.
 */

public class DeviceMemoryUtils {

    public Boolean saveToCache(File cacheDir, String response, Context context, String filename) {

        try {
            // Get instance of cache directory
            //File cacheDir = getCacheDir();
            File file = new File(cacheDir.getAbsolutePath(), filename);

            FileOutputStream fOut = new FileOutputStream(file);

            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the user requested channel string to file
            osw.write(response);
            osw.flush();
            osw.close();

            // TODO remove this toast
            Toast.makeText(context, "Saved file to cache", Toast.LENGTH_SHORT).show();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public String readFromCache(File cacheDir, String filename) {
        try {
            // Get the cache directory
            File file = new File(cacheDir, filename);
            FileInputStream fIn = new FileInputStream(file);

            InputStreamReader isr = new InputStreamReader(fIn);

            char[] inputBuffer = new char[Constants.READ_BLOCK_SIZE];
            String temp = "";
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                // Convert the read chars to a string
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                temp += readString;

                inputBuffer = new char[Constants.READ_BLOCK_SIZE];

            }
            isr.close();

            Log.d(Constants.DEBUG_TAG, "File read OK : " + temp);
            return temp;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
