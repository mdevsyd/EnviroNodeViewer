package com.dosecdesign.environodeviewer.Utitilies;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Michi on 7/05/2017.
 */

public class StringUtils {

    public String formatChannelString(String item){

        item = item.replaceAll(" ", "%20");
        return item;
    }

    public String buildString(String type, List<String> paths){
        String result = "";
        switch (type) {
            case ("channels"):
                // Create a string of all channels for http requests
                for (int i = 0; i < paths.size() - 1; i++) {
                    result = result.concat(paths.get(i).concat(","));
                }
                result = result.concat(paths.get(paths.size() - 1));
            case("timestamp"):
                // Create a string of complete start date and time for http requests
                //result = "&start="+paths.get(0)+"&end="+paths.get(1);
                //Log.d(Constants.DEBUG_TAG,"timestamp built is: "+result);
                // Verify the sting length
        }

        return result;
    }

    /**
     * Receives string input and verifies it based on required string output characteristics
     * @param string the string to be checked
     * @param type defines the expected output characteristics for the string being verified
     * @return true if string is correct, false if not
     */
    public Boolean verifyString(String string, String type){

        if(type.equals("dateTimeLength")){
            // Total timeAndDate string from user selection should be length 19
            if(string.length()==19){
                return true;
            }
        }
        else if (type.equals("timestamp")){
            byte[] timestamp = string.getBytes();
        }

        return false;
    }


}
