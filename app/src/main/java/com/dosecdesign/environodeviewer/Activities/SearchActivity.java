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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.Adapters.SiteAdapter;
import com.dosecdesign.environodeviewer.Model.JsonModel;
import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.dosecdesign.environodeviewer.Utitilies.DeviceMemoryUtils;
import com.dosecdesign.environodeviewer.Utitilies.HttpUtils;
import com.dosecdesign.environodeviewer.Utitilies.StringUtils;
import com.dosecdesign.environodeviewer.Utitilies.TimeUtils;
import com.google.gson.Gson;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView mSitesLv;
    private ListView mHubsLv;
    private ListView mInstrumentsLv;
    private ListView mChannelsLv;
    private SiteAdapter mSiteAdapter;
    private JsonModel mJsonModel;
    private HttpUtils mHttpUtil;
    private ProgressDialog mDialog;
    private JsonModel sitesJson;
    private Button mGoBtn;
    private ImageView mStartDateBtn;
    private ImageView mEndDateBtn;
    private ImageView mStartTimeBtn;
    private ImageView mEndTimeBtn;

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
    private int mStartYear, mStartMonth, mStartDay;
    private int mEndYear, mEndMonth, mEndDay;
    private int mStartHour, mStartMin;
    private int mEndHour, mEndMin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSitesLv = (ListView) findViewById(R.id.siteLv);
        mHubsLv = (ListView) findViewById(R.id.hubLv);
        mInstrumentsLv = (ListView) findViewById(R.id.instrumentLv);
        mChannelsLv = (ListView) findViewById(R.id.channelsLv);
        mGoBtn = (Button) findViewById(R.id.goBtn);
        mStartDateBtn = (ImageView) findViewById(R.id.startDateBtn);
        mStartTimeBtn = (ImageView) findViewById(R.id.startTimeBtn);
        mEndDateBtn = (ImageView) findViewById(R.id.endDateBtn);
        mEndTimeBtn = (ImageView) findViewById(R.id.endTimeBtn);

        sitesJson = new JsonModel();

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

        mStartYear = mStartMonth = mStartDay = mEndYear = mEndMonth = mEndDay =
                mStartHour = mStartMin = mEndHour = mEndMin = 0;


        if (mCachedResponse != null) {
            Log.d(Constants.DEBUG_TAG, "GetSiteData(mCachedResponse)");
            new GetSiteDataTask().execute(mCachedResponse);
        } else {
            try {
                String baseUrl = mHttpUtil.buildUrl();
                URL url = new URL(baseUrl);
                new RetrieveJsonDataTask().execute(url);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.url_unrecognised, Toast.LENGTH_SHORT).show();
            }
        }

        // Set on click listeners for date/time and GO buttons
        mStartDateBtn.setOnClickListener(this);
        mStartTimeBtn.setOnClickListener(this);
        mEndDateBtn.setOnClickListener(this);
        mEndTimeBtn.setOnClickListener(this);
        mGoBtn.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear the previously selected channel array when user presses back key
        mSelectedChannels.clear();
    }

    @Override
    public void onClick(View v) {

        // Get reference to current date and time
        final Calendar c = Calendar.getInstance();

        switch (v.getId()) {
            case R.id.startDateBtn:
                // Show date picker, save date
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
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
                        Log.d(Constants.DEBUG_TAG, "selected start date set to: " + mStartDate);
                    }
                }
                        , mYear, mMonth, mDay);
                datePickerDialog.show();
                break;

            case R.id.startTimeBtn:
                // Show time picker, save selected time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMin = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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

                    }
                }, mHour, mMin, false);
                timePickerDialog.show();
                break;

            case R.id.endDateBtn:
                // Show date picker, save date
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
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
                        Log.d(Constants.DEBUG_TAG, "selected end date set to: " + mEndDate);
                    }
                }
                        , mYear, mMonth, mDay);
                datePickerDialog.show();
                break;

            case R.id.endTimeBtn:
                // Show time picker, save selected time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMin = c.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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
                    }
                }, mHour, mMin, false);
                timePickerDialog.show();
                break;

            case R.id.goBtn:
                if (mSelectedChannels.size() != 0) {
                    String res = mHttpUtil.concatUrlQuery(mQuery, "channels", mSelectedChannels);
                    try {
                        // Build the channel string from user selected channels
                        String channelsString = Uri.encode(mStringUtils.buildString("channels", mSelectedChannels), "=,&:?()%");
                        String completeUrl = mQuery.concat(channelsString);

                        // Create timeAndDate strings from user input
                        String startDateTime = mStartDate + " " + mStartTime;
                        String endDateTime = mEndDate + " " + mEndTime;

                        Log.d(Constants.DEBUG_TAG, "start :" + startDateTime);
                        Log.d(Constants.DEBUG_TAG, "end :" + endDateTime);


                        // Verify, encode and concat timestamp strings
                        if (mStringUtils.verifyString(startDateTime, "dateTimeLength") && (mStringUtils.verifyString(endDateTime, "dateTimeLength"))) {

                            // Check selected date range
                            if (mTimeUtils.compareDates(startDateTime, endDateTime, getBaseContext())) {

                                // Date range valid, create datestamp for http request
                                String timestamp = "&start=" + startDateTime + "&end=" + endDateTime;

                                // Encode the datestamp and create a URL for http request
                                timestamp = Uri.encode(timestamp, "=,&:?()%");
                                String apiQuery = completeUrl.concat(timestamp);
                                URL url = new URL(apiQuery);
                                mDataRequestFlag = true;
                                new RetrieveJsonDataTask().execute(url);

                                Log.d(Constants.DEBUG_TAG, "Complete URL is : " + apiQuery);


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
                    }

                } else {
                    // advise user to select some channels
                    // TODO  -  before displaying toast, check if there are actually no available channels for the user to select!!
                    Toast.makeText(getApplicationContext(), R.string.select_channel, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    public class RetrieveJsonDataTask extends AsyncTask<URL, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


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
            if (result != null) {
                mDialog.dismiss();
                mResponse = result;
                // Only start GetChannelDataTask if channelSearchFlag is true, otherwise populate site, hub & inst ListViews
                if (mChannelSearchFlag) {
                    new GetChannelDataTask().execute(mResponse);
                    mChannelSearchFlag = false;
                } else if (mDataRequestFlag) {
                    mDataRequestFlag = false;

                    // CHeck if the there is data in returned data[]
                    if (checkDataArray(result)) {

                        // Data exists, save it to device cache
                        mDevMem.saveToCache(getCacheDir(), mResponse, getBaseContext(), "channels");

                        // Start plotting activity
                        Intent intent = new Intent(getBaseContext(), DataViewActivity.class);
                        intent.putExtra(Constants.SELECTED_CHANNELS, mSelectedChannels.size());
                        intent.putStringArrayListExtra(Constants.SEL_CH_ARRAY, (ArrayList<String>) mSelectedChannels);
                        startActivity(intent);
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
            }
        }
    }

    private Boolean checkDataArray(String response) {
        try {

            JSONObject responseObj = new JSONObject(response);
            JSONArray dataArray = responseObj.getJSONArray("data");
            //Log.d(Constants.DEBUG_TAG, "data in checkData :" + dataArray);
            if (dataArray.length() > 0) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

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
                mHubsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedHub = String.valueOf(parent.getItemAtPosition(position));
                        new GetInstrumentDataTask().execute(mSelectedHub);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GetInstrumentDataTask extends AsyncTask<String, String, List> {

        @Override
        protected List doInBackground(String... params) {
            String hubSerial = params[0];
            List instruments = new ArrayList();

            try {
                for (int j = 0; j < mHubsArray.length(); j++) {
                    JSONObject hub = mHubsArray.getJSONObject(j);
                    if (hub != null) {
                        if (hub.getString("serial").equals(hubSerial)) {
                            JSONObject instObject = mHubsArray.getJSONObject(j);
                            if (instObject != null) {
                                mInstrumentsArray = instObject.getJSONArray("instruments");
                                for (int k = 0; j < mInstrumentsArray.length(); k++) {
                                    JSONObject instrument = mInstrumentsArray.getJSONObject(k);
                                    if (instrument != null) {
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
                mInstrumentsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedInstrument = String.valueOf(parent.getItemAtPosition(position));
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

    public class GetChannelDataTask extends AsyncTask<String, String, List> {


        @Override
        protected List doInBackground(String... params) {

            String response = params[0];
            List channels = new ArrayList();
            mSelectedChannels.clear();


            try {
                JSONObject jObject = new JSONObject(response);
                JSONObject dataObject = jObject.getJSONObject("data");
                mChannelsArray = dataObject.getJSONArray("channels");
                if (mChannelsArray != null) {
                    for (int j = 0; j < mChannelsArray.length(); j++) {
                        JSONObject channel = mChannelsArray.getJSONObject(j);
                        if (channel != null) {
                            String channelName = channel.getString("name");
                            channels.add(channelName);
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

                // Create a list of the channels clicked
                mSelectedChannels = new ArrayList();

                mChannelsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String channel = String.valueOf(parent.getItemAtPosition(position));
                        mSelectedChannels.add(channel);
                        Log.d(Constants.DEBUG_TAG, "Selected channel (no formatting) :" + channel);
                    }
                });


            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }
            mQuery = mQuery.concat("/?channels=");

            //


            //Log.d(Constants.DEBUG_TAG, "selected channel count is "+ mSelectedChannels.size());

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Clear the previously selected channel array when user presses back key
        mSelectedChannels.clear();
    }


}
