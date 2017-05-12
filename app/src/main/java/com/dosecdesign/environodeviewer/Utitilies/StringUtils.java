package com.dosecdesign.environodeviewer.Utitilies;

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
                for (int i = 0; i < paths.size() - 1; i++) {
                    result = result.concat(paths.get(i).concat(","));
                }
                result = result.concat(paths.get(paths.size() - 1));
                return result;
        }

        return null;
    }


}
