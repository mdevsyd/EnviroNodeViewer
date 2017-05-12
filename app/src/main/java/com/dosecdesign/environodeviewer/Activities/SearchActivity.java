package com.dosecdesign.environodeviewer.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.Adapters.SiteAdapter;
import com.dosecdesign.environodeviewer.Model.JsonModel;
import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.dosecdesign.environodeviewer.Utitilies.HttpUtils;
import com.dosecdesign.environodeviewer.Utitilies.StringUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ListView mSitesLv;
    private ListView mHubsLv;
    private ListView mInstrumentsLv;
    private ListView mChannelsLv;
    private SiteAdapter mSiteAdapter;
    private JsonModel mJsonModel;
    private Gson mGson;
    private HttpUtils mHttpUtil;
    private ProgressDialog mDialog;
    private JsonModel sitesJson;
    private Button mGoBtn;

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

    private List mSelectedChannels;

    private StringUtils mStringUtils;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSitesLv = (ListView) findViewById(R.id.siteLv);
        mHubsLv = (ListView) findViewById(R.id.hubLv);
        mInstrumentsLv = (ListView) findViewById(R.id.instrumentLv);
        mChannelsLv = (ListView) findViewById(R.id.channelsLv);
        mGoBtn = (Button) findViewById(R.id.goBtn);

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
        mQuery= "";

        String baseUrl = mHttpUtil.buildUrl();
        try {
            URL url = new URL(baseUrl);
            new RetrieveJsonDataTask().execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.url_unrecognised, Toast.LENGTH_SHORT).show();
        }
        mGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedChannels !=null){
                    String res = mHttpUtil.concatUrlQuery(mQuery, "channels", mSelectedChannels);
                    try {
                        String channelsString = Uri.encode(mStringUtils.buildString("channels", mSelectedChannels), "=,&:?()%");
                        Log.d(Constants.DEBUG_TAG," Encoded test url: "+channelsString);
                        String completeUrl = mQuery.concat(channelsString);

                        // TODO add a date range
                        String urlWithStartDate = completeUrl.concat("&start=2017-04-11%2009:00:00");
                        URL url = new URL(urlWithStartDate);
                        mDataRequestFlag = true;
                        new RetrieveJsonDataTask().execute(url);

                        Log.d(Constants.DEBUG_TAG, "Complete URL is : "+completeUrl);
                        Log.d(Constants.DEBUG_TAG, "Complete URL is : "+ urlWithStartDate);

                        //clear the channels listview

                        //new RetrieveJsonDataTask().execute(requestURL);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    //Log.d(Constants.DEBUG_TAG, "new url is :"+ res);
                } else{
                    // advise user to select some channels
                    // TODO  -  before displaying toast, check if there are actually no available channels for the user to select!!
                    Toast.makeText(getApplicationContext(), R.string.select_channel, Toast.LENGTH_SHORT).show();
                }
            }
        });


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
            Log.d(Constants.DEBUG_TAG, "RetrieveJsonTask url :"+url);

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
                    mChannelSearchFlag =false;
                } else if(mDataRequestFlag){
                    Log.d(Constants.DEBUG_TAG, "Fantastic");
                    mDataRequestFlag=false;

                    // Save the returned data to device cache
                    saveToCache(mResponse);

                    // Start plotting activity
                    Intent intent = new Intent(getBaseContext(),DataViewActivity.class);
                    startActivity(intent);

                }

                else {
                    new GetSiteDataTask().execute(mResponse);

                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.http_error, Toast.LENGTH_SHORT).show();
            }
        }
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
                Log.d(Constants.DEBUG_TAG, "sites length is: " + sitesArray.length());

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
                        Log.d(Constants.DEBUG_TAG, "Site is: " + mSelectedSite);
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
                        /*String correctedChannel = mStringUtils.formatChannelString(String.valueOf(parent.getItemAtPosition(position)));
                        mSelectedChannels.add(correctedChannel);
                        Log.d(Constants.DEBUG_TAG, "Selected channel: "+ correctedChannel);*/
                        String channel = String.valueOf(parent.getItemAtPosition(position));
                        mSelectedChannels.add(channel);
                        Log.d(Constants.DEBUG_TAG,"Selected channel (no formatting) :" + channel);
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

    public void saveToCache(String response){

        try{
            // Get instance of cache directory
            File cacheDir = getCacheDir();
            File file = new File(cacheDir.getAbsolutePath(), "requested_data.txt");

            FileOutputStream fOut =new FileOutputStream(file);

            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the user requested channel string to file
            osw.write(response);
            osw.flush();
            osw.close();

            // TODO remove this toast
            Toast.makeText(getBaseContext(), "Saved file to cache", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
