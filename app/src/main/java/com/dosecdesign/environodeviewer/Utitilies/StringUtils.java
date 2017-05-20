package com.dosecdesign.environodeviewer.Utitilies;


import java.util.List;

/**
 * Utility class for String manipulation.
 */

public class StringUtils {

    /**
     * Build a string of different types
     *
     * @param type  the type to be built
     * @param paths the string paths to concatenate
     * @return the built string
     */
    public String buildString(String type, List<String> paths) {
        String result = "";
        switch (type) {
            case ("channels"):
                // Create a string of all channels for http requests
                for (int i = 0; i < paths.size() - 1; i++) {
                    result = result.concat(paths.get(i).concat(","));
                }
                result = result.concat(paths.get(paths.size() - 1));
        }

        return result;
    }

    /**
     * Receives string input and verifies it based on required string output characteristics
     *
     * @param string the string to be checked
     * @param type   defines the expected output characteristics for the string being verified
     * @return true if string is correct, false if not
     */
    public Boolean verifyString(String string, String type) {

        if (type.equals("dateTimeLength")) {
            // Total timeAndDate string from user selection should be length 19
            if (string.length() == 19) {
                return true;
            }
        }

        return false;
    }


}
