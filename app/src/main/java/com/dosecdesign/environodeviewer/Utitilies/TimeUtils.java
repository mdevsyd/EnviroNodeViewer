package com.dosecdesign.environodeviewer.Utitilies;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public String getFormattedCurrentDateTime(){
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatter.format(today);
    }

    public Date addOrSubDays(int numDays, Date oldDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(oldDate);
        cal.add(Calendar.DATE, numDays);
        return cal.getTime();
    }

    public Date addOrSubMonths(int numMonths, Date oldDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(oldDate);
        cal.add(Calendar.MONTH, numMonths);
        return cal.getTime();

    }
    public Date addOrSubYears(int numYears, Date oldDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(oldDate);
        cal.add(Calendar.YEAR, numYears);
        return cal.getTime();

    }

    public String formatDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatter.format(date);
    }

    /**
     * compares two dates to check start is before end
     * @param startDateTime user selected start date
     * @param endDateTime user selected end date
     * @return true if start is before end, false if not
     */
    public Boolean compareDates(String startDateTime, String endDateTime, Context c) {

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startDateTime);
            Date end = sdf.parse(endDateTime);

            if(start.before(end)){
                return true;
            }
            else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(c, R.string.try_again, Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
