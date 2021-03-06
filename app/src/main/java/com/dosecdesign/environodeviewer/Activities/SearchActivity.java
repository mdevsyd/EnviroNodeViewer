package com.dosecdesign.environodeviewer.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.Adapters.SiteAdapter;
import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.dosecdesign.environodeviewer.Utitilies.DeviceMemoryUtils;
import com.dosecdesign.environodeviewer.Utitilies.HttpUtils;
import com.dosecdesign.environodeviewer.Utitilies.StringUtils;
import com.dosecdesign.environodeviewer.Utitilies.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Activity to allow user to select  site, hub, instrument and channel they wish to query the server for.
 *
 */

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView mSitesLv;
    private ListView mHubsLv;
    private ListView mInstrumentsLv;
    private ListView mChannelsLv;
    private SiteAdapter mSiteAdapter;
    private HttpUtils mHttpUtil;
    private ProgressDialog mDialog;
    private Button mGoBtn;
    private Button mClearCh;
    private Button m24HrBtn;
    private Button m7DayBtn;
    private Button mMonthBtn;
    private Button mYearBtn;

    private ImageView mStartDateBtn;
    private ImageView mEndDateBtn;
    private ImageView mStartTimeBtn;
    private ImageView mEndTimeBtn;

    private TextView mStartRes;
    private TextView mEndRes;
    private TextView mHubNameTv, mInstNameTv, mChNameTv, mDateTimeTv;

    private int mDay, mMonth, mYear, mHour, mMin;

    private String mResponse;

    private String mSelectedSite;
    private String mSelectedHub;
    private String mSelectedInstrument;

    private JSONArray mSitesArray;
    private JSONArray mHubsArray;
    private JSONArray mInstrumentsArray;
    private JSONArray mChannelsArray;

    private Boolean mChannelSearchFlag;
    private Boolean mDataRequestFlag;

    private ArrayList mSelectedChannels;

    private StringUtils mStringUtils;
    private TimeUtils mTimeUtils;
    private String mQuery;

    private DeviceMemoryUtils mDevMem;

    private String mCachedResponse;
    private String mStartTime;
    private String mStartDate;
    private String mEndTime;
    private String mEndDate;
    private String mStartDateStamp;
    private String mEndDateStamp;

    private int mStartYear, mStartMonth, mStartDay;
    private int mEndYear, mEndMonth, mEndDay;
    private int mStartHour, mStartMin;
    private int mEndHour, mEndMin;

    private Boolean[] mActionReg;
    private Boolean mHttpError;
    private URL mUrl;


    /**
     * Sets up the views and buttons of this activity. Requests the site data for account 53 (demo)
     * and click listeners for all clickeable items in the view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSitesLv = (ListView) findViewById(R.id.siteLv);
        mHubsLv = (ListView) findViewById(R.id.hubLv);
        mInstrumentsLv = (ListView) findViewById(R.id.instrumentLv);
        mChannelsLv = (ListView) findViewById(R.id.channelsLv);
        mGoBtn = (Button) findViewById(R.id.goBtn);
        mClearCh = (Button) findViewById(R.id.clearChBtn);
        m24HrBtn = (Button) findViewById(R.id.last24Btn);
        m7DayBtn = (Button) findViewById(R.id.last7DaysBtn);
        mMonthBtn = (Button) findViewById(R.id.lastMonthBtn);
        mYearBtn = (Button) findViewById(R.id.last365DaysBtn);
        mStartDateBtn = (ImageView) findViewById(R.id.startDateBtn);
        mStartTimeBtn = (ImageView) findViewById(R.id.startTimeBtn);
        mEndDateBtn = (ImageView) findViewById(R.id.endDateBtn);
        mEndTimeBtn = (ImageView) findViewById(R.id.endTimeBtn);
        mStartRes = (TextView)findViewById(R.id.startResult);
        mEndRes  = (TextView)findViewById(R.id.endResult);
        mHubNameTv = (TextView)findViewById(R.id.hubNameTv);
        mInstNameTv = (TextView) findViewById(R.id.instrumentNameTv);
        mChNameTv = (TextView) findViewById(R.id.chNameTv);
        mDateTimeTv = (TextView) findViewById(R.id.dateTimeTv);

        mResponse = "";

        mHttpUtil = new HttpUtils();

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);


        mSelectedSite = "";
        mSelectedHub = "";
        mSelectedInstrument = "";


        mSitesArray = new JSONArray();
        mHubsArray = new JSONArray();
        mInstrumentsArray = new JSONArray();
        mChannelsArray = new JSONArray();

        mChannelSearchFlag = false;
        mDataRequestFlag = false;
        mSelectedChannels = new ArrayList();

        mStringUtils = new StringUtils();
        mTimeUtils = new TimeUtils();
        mQuery = "";

        mDevMem = new DeviceMemoryUtils();

        mCachedResponse = mDevMem.readFromCache(getCacheDir(), "site");


        mEndDate = mTimeUtils.getFormattedCurrentDateTime();
        mStartDate = mTimeUtils.formatDate(mTimeUtils.addOrSubDays(-1, Calendar.getInstance().getTime()));
        Log.d(Constants.DEBUG_TAG,"date defaults: "+mStartDate+" "+mEndDate);
        mStartTime = "00:00:00";
        mEndTime = "00:00:00";
        mStartDateStamp = "";
        mEndDateStamp="";
        mStartYear = mStartMonth = mStartDay = mEndYear = mEndMonth = mEndDay =
                mStartHour = mStartMin = mEndHour = mEndMin = 0;
        initialiseActionRegister();

        // Initialise mURL
        try {
            mUrl = new URL("");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        mHttpError=false;
        if (mCachedResponse != null) {
            //
        } if (1>0){
            try {
                String baseUrl = mHttpUtil.buildUrl();
                URL url = new URL(baseUrl);
                new RetrieveJsonDataTask().execute(url);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.url_unrecognised, Toast.LENGTH_SHORT).show();
            }
        }

        // Set on click listeners for all buttons
        mStartDateBtn.setOnClickListener(this);
        mStartTimeBtn.setOnClickListener(this);
        mEndDateBtn.setOnClickListener(this);
        mEndTimeBtn.setOnClickListener(this);
        mGoBtn.setOnClickListener(this);
        mClearCh.setOnClickListener(this);
        m24HrBtn.setOnClickListener(this);
        m7DayBtn.setOnClickListener(this);
        mMonthBtn.setOnClickListener(this);
        mYearBtn.setOnClickListener(this);


    }

    /**
     * Initialise the action register to zeros
     */
    private void initialiseActionRegister() {
        // 16 int array is to set certain actions within this activity
        // Initialise all to false
        mActionReg = new Boolean[16];
        for (int i =0;i<mActionReg.length; i++){
            mActionReg[i]=false;
        }
    }

    /**
     * Clear the selected channels for when a user returns to the view after graphing
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Clear the previously selected channel array when user presses back key
        mSelectedChannels.clear();
    }

    /**
     * Actions for each clickeable item in the view
     * @param v - the view id for the clicked item
     */
    @Override
    public void onClick(View v) {
        Date newDate = null;

        // Get reference to current date and time
        final Calendar c = Calendar.getInstance();

        switch (v.getId()) {
            case R.id.startDateBtn:
                mActionReg[0]=true;
                // Show date picker, save date
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // User has selected start date, set reg bit
                        mActionReg[1]=true;
                        mStartYear = year;
                        mStartMonth = monthOfYear;
                        mStartDay = dayOfMonth;
                        int month = monthOfYear + 1;
                        String monthString = String.valueOf(month);
                        if (month < 10) {
                            monthString = "0" + String.valueOf(month);
                        }
                        String dayString = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) {
                            dayString = "0" + String.valueOf(dayOfMonth);
                        }
                        mStartDate = year + "-" + monthString + "-" + dayString;
                        setDateResult();
                    }
                }
                        , mYear, mMonth, mDay);
                datePickerDialog.show();
                break;

            case R.id.startTimeBtn:
                mActionReg[0]=true;
                // Show time picker, save selected time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMin = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // User has selected start time, set reg bit
                        mActionReg[2]=true;
                        mStartHour = hourOfDay;
                        mStartMin = minute;
                        String hourString = String.valueOf(hourOfDay);
                        String minString = String.valueOf(minute);
                        if (hourOfDay < 10) {
                            hourString = "0" + hourOfDay;
                        }
                        if (minute < 10) {
                            minString = "0" + minute;
                        }
                        mStartTime = hourString + ":" + minString + ":" + "00";
                        Log.d(Constants.DEBUG_TAG, "start time selected: " + mStartTime);
                        setDateResult();

                    }
                }, mHour, mMin, false);
                timePickerDialog.show();
                break;

            case R.id.endDateBtn:
                mActionReg[0]=true;
                // Show date picker, save date
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // User has selected end date, set reg bit
                        mActionReg[3]=true;
                        mEndYear = year;
                        mEndMonth = monthOfYear;
                        mEndDay = dayOfMonth;
                        int month = monthOfYear + 1;
                        String monthString = String.valueOf(month);
                        if (month < 10) {
                            monthString = "0" + String.valueOf(month);
                        }
                        String dayString = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) {
                            dayString = "0" + String.valueOf(dayOfMonth);
                        }
                        mEndDate = year + "-" + monthString + "-" + dayString;
                        setDateResult();

                    }
                }
                        , mYear, mMonth, mDay);
                datePickerDialog.show();
                break;

            case R.id.endTimeBtn:
                mActionReg[0]=true;
                // Show time picker, save selected time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMin = c.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // User has selected end time, set reg bit
                        mActionReg[4]=true;
                        mEndHour = hourOfDay;
                        mEndMin = minute;
                        // Format values for use in http request
                        String hourString = String.valueOf(hourOfDay);
                        String minString = String.valueOf(minute);
                        if (hourOfDay < 10) {
                            hourString = "0" + hourOfDay;
                        }
                        if (minute < 10) {
                            minString = "0" + minute;
                        }
                        mEndTime = hourString + ":" + minString + ":" + "00";
                        Log.d(Constants.DEBUG_TAG, "end time selected: " + mEndTime);
                        setDateResult();

                    }
                }, mHour, mMin, false);
                timePickerDialog.show();
                break;

            case R.id.goBtn:
                if(mSelectedChannels.size()>1){
                    Toast.makeText(SearchActivity.this, "Please select ONE channel only", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mSelectedChannels.size() != 0) {
                    //String res = mHttpUtil.concatUrlPath(mQuery, "channels", mSelectedChannels);
                    try {
                        // Build the channel string from user selected channels
                        String channelsString = Uri.encode(mStringUtils.buildString("channels", mSelectedChannels), "=,&:?()%");
                        String urlWithChannels = mQuery.concat(channelsString);
                        String startDateTime;
                        String endDateTime;

                        // Check bits of action register
                        if(mActionReg[0]) {
                            // User has entered custom dates, create timestamp strings
                            startDateTime = mStartDate + " " + mStartTime;
                            endDateTime = mEndDate + " " + mEndTime;

                            // Clear the status flag in actionReg
                            mActionReg[0]=false;

                        }
                        else{
                            // User has chosen a predefined date range, create timestamp strings
                            startDateTime = mStartDateStamp;
                            endDateTime = mEndDateStamp;
                        }


                        // Verify, encode and concat timestamp strings
                        if (mStringUtils.verifyString(startDateTime, "dateTimeLength") && (mStringUtils.verifyString(endDateTime, "dateTimeLength"))) {

                            // Check selected date range
                            if (mTimeUtils.compareDates(startDateTime, endDateTime, getBaseContext())) {

                                // Date range valid, create datestamp for http request
                                String timestamp = "&start=" + startDateTime + "&end=" + endDateTime;

                                // Encode the datestamp and create a URL for http request
                                timestamp = Uri.encode(timestamp, "=,&:?()%");
                                String apiQuery = urlWithChannels.concat(timestamp);
                                URL url = new URL(apiQuery);
                                mUrl = url;
                                mDataRequestFlag = true;

                                Log.d(Constants.DEBUG_TAG, "Hub is: "+mSelectedHub+", Device is: "+mSelectedInstrument+", channel is: "+mSelectedChannels.get(0)+",\n start date/time is: "+startDateTime+", end date/time is: "+endDateTime);
                                new RetrieveJsonDataTask().execute(url);
                                if(mHttpError){
                                    new RetrieveJsonDataTask().execute(mUrl);
                                }


                            } else {
                                // User has input invalid date range
                                Toast.makeText(getBaseContext(), R.string.invalid_date_range, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // A formatting error has occurred
                            Log.e(Constants.ERROR_TAG, "Error formatting date/time");

                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.e(Constants.ERROR_TAG, "Malformed URL after clicking GoBtn in SearchAvtivity");
                    }

                } else if((mSelectedChannels.size()==0) || (mSelectedHub==null) || (mSelectedInstrument==null)){
                    // advise user to select some channels
                    // TODO  -  before displaying toast, check if there are actually no available channels for the user to select!!
                    Toast.makeText(getApplicationContext(), R.string.select_channel, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.clearChBtn:
                mSelectedChannels.clear();
                mChNameTv.setText("");
                break;
            case R.id.last24Btn:
                // Subtract one day from today
                newDate = mTimeUtils.addOrSubDays(-1, Calendar.getInstance().getTime());
                mStartDateStamp = mTimeUtils.formatDate(newDate);
                mEndDateStamp = mTimeUtils.getFormattedCurrentDateTime();

                mDateTimeTv.setText(mStartDateStamp+" to "+mEndDateStamp);
                break;
            case R.id.last7DaysBtn:
                // Subtract 7 days from today
                newDate = mTimeUtils.addOrSubDays(-7, Calendar.getInstance().getTime());
                mStartDateStamp = mTimeUtils.formatDate(newDate);
                mEndDateStamp = mTimeUtils.getFormattedCurrentDateTime();
                Log.d(Constants.DEBUG_TAG,"date :"+mStartDateStamp+" to "+mEndDateStamp);
                mDateTimeTv.setText(mStartDateStamp+" to "+mEndDateStamp);
                break;
            case R.id.lastMonthBtn:
                // Subtract 1 month from today
                newDate = mTimeUtils.addOrSubMonths(-1, Calendar.getInstance().getTime());
                mStartDateStamp = mTimeUtils.formatDate(newDate);
                mEndDateStamp = mTimeUtils.getFormattedCurrentDateTime();
                Log.d(Constants.DEBUG_TAG,"date :"+mStartDateStamp+" to "+mEndDateStamp);
                mDateTimeTv.setText(mStartDateStamp+" to "+mEndDateStamp);
                break;
            case R.id.last365DaysBtn:
                // Subtract 1 year from today
                newDate = mTimeUtils.addOrSubYears(-1, Calendar.getInstance().getTime());
                mStartDateStamp = mTimeUtils.formatDate(newDate);
                mEndDateStamp = mTimeUtils.getFormattedCurrentDateTime();
                Log.d(Constants.DEBUG_TAG,"date :"+mStartDateStamp+" to "+mEndDateStamp);
                mDateTimeTv.setText(mStartDateStamp+" to "+mEndDateStamp);
                break;


        }
    }


    /**
     * Async tasks that takes a URL parameter and queries the server using an HTTP connection. Upon
     * success, it initiates the next task based on a set flag.
     */
    public class RetrieveJsonDataTask extends AsyncTask<URL, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            mHttpError =false;

            if (mChannelSearchFlag) {
                mDialog.setMessage("Retrieving Channel data for " + mSelectedInstrument);
                mDialog.show();
            } else {
                mDialog.setMessage("Contacting Site, Please Wait...");
                mDialog.show();
            }

        }

        HttpURLConnection mConnection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(URL... params) {



            URL url = params[0];

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
                Log.e(Constants.ERROR_TAG,"IO Exception in RetrieveJsonDataTask, while trying to read inputstream");
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
                    Log.e(Constants.ERROR_TAG,"IO Exception in RetrieveJsonDataTask, while attempting to close BufferedReader");
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                mDialog.dismiss();
                mResponse = result;
                // Only start GetChannelDataTask if channelSearchFlag is true, otherwise populate site, hub & inst ListViews
                if (mChannelSearchFlag) {
                    new GetChannelDataTask().execute(mResponse);
                    mChannelSearchFlag = false;
                } else if (mDataRequestFlag) {
                    mDataRequestFlag = false;

                    // Check if the there is data in returned data[]
                    if (checkDataArray(result)) {

                        // Data exists, save it to device cache
                        mDevMem.saveToCache(getCacheDir(), mResponse, getBaseContext(), "channels");

                        try {

                            // Extract each data object's datestamp and save to array for plotting activity

                            // create JSON object (result is returned from server query)
                            JSONObject jObject = new JSONObject(result);
                            JSONArray dataArray  = jObject.getJSONArray("data");
                            String[] dateList = new String[dataArray.length()];

                            for (int i=0;i<dataArray.length();i++){
                                JSONObject dataObj = dataArray.getJSONObject(i);
                                dateList[i]=dataObj.getString("DateAndTime");
                            }

                            // Pass data to and start plotting activity
                            Intent intent = new Intent(getBaseContext(), DataViewActivity.class);
                            // add the amount of channels selected and the channel
                            intent.putExtra(Constants.SELECTED_CHANNELS, mSelectedChannels.size());
                            intent.putStringArrayListExtra(Constants.SEL_CH_ARRAY, (ArrayList<String>) mSelectedChannels);
                            // create a bundle and add the string array containing date stamps
                            Bundle b = new Bundle();
                            b.putStringArray(Constants.DATESTAMP_ARRAY,dateList);
                            b.putString(Constants.SELECTED_HUB, mSelectedHub);
                            b.putString(Constants.SELECTED_INSTRUMENT, mSelectedInstrument);
                            b.putString(Constants.SELECTED_CHANNEL_NAME, String.valueOf(mSelectedChannels.get(0)));
                            // add bundle to intent
                            intent.putExtras(b);
                            // Reset action register before starting next activity
                            initialiseActionRegister();
                            // start activity
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(Constants.ERROR_TAG,"JSON Exception  in RetrieveJsonDataTask, while trying to create JSON object");
                        }catch (RuntimeException e){
                            e.printStackTrace();
                            Log.e(Constants.ERROR_TAG,"Runtime Exception in RetrieveJsonDataTask, while trying access JSON array");
                        }


                    }

                    //TODO makes this into a dialog instead of toast.
                    else {
                        Toast.makeText(getBaseContext(), R.string.no_data_exists, Toast.LENGTH_LONG).show();
                    }
                } else {
                    new GetSiteDataTask().execute(mResponse);

                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.http_error, Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
                Toast.makeText(SearchActivity.this, "Please wait, re-attempting your request",Toast.LENGTH_SHORT).show();
                mHttpError =true;
            }
        }
    }

    /**
     * Method to check if any data exists inside the API response
     * @param response - the String containing returned data from API call
     * @return - true if data exists, false if not
     */
    private Boolean checkDataArray(String response) {
        try {
            // create JSON object from the API response
            JSONObject responseObj = new JSONObject(response);
            // get access to the data array
            JSONArray dataArray = responseObj.getJSONArray("data");
            // check array length
            if (dataArray.length() > 0) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.ERROR_TAG,"JSON Exception in checkDataArray while trying to create JSON object");
        }
        return false;
    }

    /**
     * Async task updates the UI with site data as per response string sent this method call.
     * Upon success, the data is updated on the UI using the adapter and a item click listener is set to
     * react to user selection
     */
    public class GetSiteDataTask extends AsyncTask<String, String, List> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Retrieving Your Data...");

        }

        @Override
        protected List doInBackground(String... params) {

            String response = params[0];
            List sites = new ArrayList();

            try {
                JSONObject jObject = new JSONObject(response);
                JSONObject sitesObject = jObject.getJSONObject("data");
                JSONArray sitesArray = sitesObject.getJSONArray("sites");
                // Copy array to a local variable
                mSitesArray = sitesArray;

                for (int i = 0; i < sitesArray.length(); i++) {
                    // Get reference to each site array object
                    JSONObject site = sitesArray.getJSONObject(i);
                    // Add each site to the site list
                    sites.add(site.getString("name"));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return sites;

        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {
                SiteAdapter siteAdapter = new SiteAdapter(SearchActivity.this, list);
                mSitesLv.setAdapter(siteAdapter);
                mSitesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        mSelectedSite = String.valueOf(parent.getItemAtPosition(position));
                        new GetHubDataTask().execute(mResponse, (String.valueOf(position)));
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }


        }
    }
    /**
     * Async task updates the UI with hub data as per response string sent this method call.
     * Upon success, the data is updated on the UI using the adapter and a item click listener is set to
     * react to user selection
     */
    public class GetHubDataTask extends AsyncTask<String, String, List> {

        @Override
        protected List doInBackground(String... params) {
            String response = params[0];
            List hubs = new ArrayList();

            try {

                if (mSitesArray != null) {
                    // Go through each site to get hubs
                    for (int i = 0; i < mSitesArray.length(); i++) {
                        // Get the array of hubs for selected site (only ever one site at the moment)
                        JSONObject hubObject = mSitesArray.getJSONObject(i);
                        if (hubObject != null) {
                            // Store local copy of hubs array
                            mHubsArray = hubObject.getJSONArray("hubs");
                            if (mHubsArray != null) {
                                for (int j = 0; j < mHubsArray.length(); j++) {
                                    JSONObject hub = mHubsArray.getJSONObject(j);
                                    if (hub != null) {
                                        String hubSerial = hub.getString("serial");
                                        hubs.add(hubSerial);
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return hubs;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {
                SiteAdapter hubAdapter = new SiteAdapter(SearchActivity.this, list);
                mHubsLv.setAdapter(hubAdapter);
                setListViewHeight(mHubsLv);

                // Make the lv scroll inside the overall scrollview
                // source: http://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view
                mHubsLv.setOnTouchListener(new View.OnTouchListener() {
                    // Setting on Touch Listener for handling the touch inside ScrollView
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Disallow the touch request for parent scroll on touch of child view
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;

                    }
                });

                mHubsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedHub = String.valueOf(parent.getItemAtPosition(position));
                        mHubNameTv.setText(mSelectedHub);
                        new GetInstrumentDataTask().execute(mSelectedHub);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Async task updates the UI with  Instrument data as per response string sent this method call.
     * Upon success, the data is updated on the UI using the adapter and a item click listener is set to
     * react to user selection
     */
    public class GetInstrumentDataTask extends AsyncTask<String, String, List> {

        @Override
        protected List doInBackground(String... params) {
            String hubSerial = params[0];
            List instruments = new ArrayList();

            try {
                for (int j = 0; j < mHubsArray.length(); j++) {
                    JSONObject hub = mHubsArray.getJSONObject(j);
                    if (hub != null) {
                        // check if serial is the one user has clicked
                        if (hub.getString("serial").equals(hubSerial)) {
                            JSONObject instObject = mHubsArray.getJSONObject(j);
                            if (instObject != null) {
                                // get reference to the instruments array
                                mInstrumentsArray = instObject.getJSONArray("instruments");
                                for (int k = 0; k < mInstrumentsArray.length(); k++) {
                                    // get reference to instrument object
                                    JSONObject instrument = mInstrumentsArray.getJSONObject(k);
                                    if (instrument != null) {
                                        // get instrument serial, add to instruments list
                                        String instSerial = instrument.getString("serial");
                                        instruments.add(instSerial);
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return instruments;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {
                SiteAdapter siteAdapter = new SiteAdapter(SearchActivity.this, list);
                mInstrumentsLv.setAdapter(siteAdapter);
                setListViewHeight(mInstrumentsLv);

                // Allow for lv touch by overriding scrollview touch
                mInstrumentsLv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Disallow the touch request for parent scroll on touch of child view
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;

                    }
                });
                mInstrumentsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedInstrument = String.valueOf(parent.getItemAtPosition(position));
                        mInstNameTv.setText(mSelectedInstrument);
                        try {
                            URL instrumentUrl = new URL(mHttpUtil.concatUrlPath(mSelectedInstrument));
                            mQuery = instrumentUrl.toString();
                            mChannelSearchFlag = true;
                            new RetrieveJsonDataTask().execute(instrumentUrl);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Async task updates the UI with channel data as per response string sent this method call.
     * Upon success, the data is updated on the UI using the adapter and a item click listener is set to
     * react to user selection
     */
    public class GetChannelDataTask extends AsyncTask<String, String, List> {


        @Override
        protected List doInBackground(String... params) {

            String response = params[0];
            List channels = new ArrayList();
            mSelectedChannels.clear();


            try {
                // create JSON object from the passed parameter
                JSONObject jObject = new JSONObject(response);
                // get reference to the data object
                JSONObject dataObject = jObject.getJSONObject("data");
                // get reference to the channels array
                mChannelsArray = dataObject.getJSONArray("channels");
                if (mChannelsArray != null) {
                    // get each channel name in channels array, add it to channels arrayList
                    for (int j = 0; j < mChannelsArray.length(); j++) {
                        JSONObject channel = mChannelsArray.getJSONObject(j);
                        if (channel != null) {
                            String channelName = channel.getString("name");
                            // do not show DateAndTime channel as it is not plottable
                            if(!channelName.equals("DateAndTime")){
                                channels.add(channelName);
                            }

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return channels;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {

                final SiteAdapter channelsAdapter = new SiteAdapter(SearchActivity.this, list);
                mChannelsLv.setAdapter(channelsAdapter);
                setListViewHeight(mChannelsLv);
                mChannelsLv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Disallow the touch request for parent scroll on touch of child view
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });

                // Create a list for channels clicked on
                mSelectedChannels = new ArrayList();

                mChannelsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String channel = String.valueOf(parent.getItemAtPosition(position));
                        mChNameTv.setText(channel);
                        mSelectedChannels.clear();
                        mSelectedChannels.add(channel);
                    }
                });


            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }
            // Append paths to the query string: channels and DateAndTime will always be in query url
            mQuery = mQuery.concat("/?channels=DateAndTime,");
        }
    }

    /**
     * Clears the selected channel list and name when user returns to previous activity
     *
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Clear the previously selected channel array when user presses back key
        mSelectedChannels.clear();
        mChNameTv.setText("");
    }

    /**
     * Method required to allow a listview inside a scrollview.
     * @param listView -  the listview the user wants to scroll inside of the scrollview
     */
    public static void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * Set the date variables to use and update the UI
     */
    public void setDateResult(){
        if(mActionReg[1] || mActionReg[2] || mActionReg[3] || mActionReg[4]){
            String startDateTime = mStartDate + " " + mStartTime;
            String endDateTime = mEndDate + " " + mEndTime;
            /*mStartRes.setText(startDateTime);
            mEndRes.setText(endDateTime);*/
            Log.d(Constants.DEBUG_TAG,"date :"+mStartDate + " " + mStartTime+" to "+mEndDate + " " + mEndTime);
            mDateTimeTv.setText(startDateTime+" to "+endDateTime);
        }

    }

}
