package com.dosecdesign.environodeviewer.Utitilies;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michi on 23/04/2017.
 */

public class TimeUtils {

    /**
     * Return current Android System time as dictated by devices current settings.
     * @return
     */
    public Date getCurrentLocalTime(){

        return Calendar.getInstance().getTime();
    }

    public Date addOrSubDays(int numDays, Date oldDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(oldDate);
        cal.add(Calendar.DATE, numDays);
        return cal.getTime();
    }
}
