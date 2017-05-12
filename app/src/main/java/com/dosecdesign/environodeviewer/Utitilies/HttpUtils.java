package com.dosecdesign.environodeviewer.Utitilies;

import android.net.Uri;
import android.util.Log;

import java.net.URL;
import java.util.List;

/**
 * Created by Michi on 5/05/2017.
 */

public class HttpUtils {

    // https://api.ictcommunity.org/v0/Accounts/A3JHBLG1ZBYLK63Q

    public String buildUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(Constants.HTTP_SCHEME)
                .authority(Constants.HTTP_AUTHORITY)
                .appendPath(Constants.HTTP_ICT_PATH_1)
                .appendPath(Constants.HTTP_ICT_PATH_2)
                .appendPath(Constants.HTTP_ICT_PATH_3);
        Log.d(Constants.DEBUG_TAG, " String built is: "+ builder.build().toString());

        return builder.build().toString();
    }

    public String buildNewUrl(List<String> paths, String queryType, List<String> queries, List<String> fragments){

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(Constants.HTTP_SCHEME)
                .authority(Constants.HTTP_AUTHORITY)
                .appendPath(Constants.HTTP_ICT_PATH_1)
                .appendPath(Constants.HTTP_ICT_PATH_2)
                .appendPath(Constants.HTTP_ICT_PATH_3);
        for(String path : paths){
            builder.appendPath(path);
        }
        switch (queryType){
            case ("channels"):
                builder.appendPath("?channels=");
                for(String path : paths){
                    builder.appendPath(path);
                }
        }


                //TODO finish this - too hard!!!
        return null;
    }
    public String concatUrlPath(String path){

        return buildUrl().concat("/").concat(path);
    }

    public String encodeUri(String base, String query){

        return base.concat(query);
    }

    public String concatUrlQuery(String base, String type, List<String> channels){

        String baseUrl = base;
        switch (type){
            case ("channels"):
                baseUrl=baseUrl.concat("/?channels=");

                for (int i=0;i<channels.size()-1;i++){
                    baseUrl = baseUrl.concat(channels.get(i).concat(","));
                }
                baseUrl = baseUrl.concat(channels.get(channels.size()-1));
                //String encoded = Uri.encode(baseUrl);
                return baseUrl;
        }
       return null;
    }
}
