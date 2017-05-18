package com.dosecdesign.environodeviewer.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataViewActivity extends AppCompatActivity {

    private String mCachedResponse;

    private LineChart mLineChart1;
    private LineChart mLineChart2;
    private LineChart mLineChart3;
    private LineChart mLineChart4;

    private ArrayList mValues;
    private ArrayList mSelChannels;
    private ArrayList mList;
    private ArrayList mAllData;
    private ArrayList mEntries;
    private ArrayList mAllEntries;
    private List<ILineDataSet> mDataSets;
    private LineData mData;
    private LineDataSet mSet;
    private List mColours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        mLineChart1 = (LineChart) findViewById(R.id.dataPlot1);
        mLineChart2 = (LineChart) findViewById(R.id.dataPlot2);
        mLineChart3 = (LineChart) findViewById(R.id.dataPlot3);
        mLineChart4 = (LineChart) findViewById(R.id.dataPlot4);

        // Read from the cache
        mCachedResponse = readFromCache();

        mValues = new ArrayList<>();
        mSelChannels = new ArrayList();
        mList = new ArrayList<>();
        mAllData = new ArrayList();
        mEntries = new ArrayList<>();
        mAllEntries = new ArrayList();

        mDataSets = new ArrayList<>();
        mData = new LineData();

        mSet = new LineDataSet(null, null);
        mColours = new ArrayList<Integer>();

        Bundle extras = getIntent().getExtras();
        mSelChannels = extras.getStringArrayList(Constants.SEL_CH_ARRAY);

        // Create an array of colours - 12 repeated colours currently
        for(int i=0; i<3;i++){
            mColours.add(R.color.graphBlue);
            mColours.add(R.color.graphGreen);
            mColours.add(R.color.graphRed);
            mColours.add(R.color.graphYellow);
        }

        getValuesFromResponse(extras.getInt(Constants.SELECTED_CHANNELS), mSelChannels);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Clear the response data when user presses the back key
        mAllData.clear();
    }

    private String readFromCache() {
        try {
            // Get the cache directory
            File cacheDir = getCacheDir();
            File file = new File(cacheDir, "requested_data.txt");
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

    /**
     * Create entries for line chart
     *
     * @param numCh   amount of channels selected by user
     * @param chNames names of channels selected by user
     * @return arraylist of entry values for plotting
     */
    public ArrayList<Entry> getValuesFromResponse(int numCh, ArrayList<String> chNames) {
        try {
            // Get channel data to plot
            JSONObject responseObj = new JSONObject(mCachedResponse);
            JSONArray dataArray = responseObj.getJSONArray("data");

            for (int i=0; i<numCh; i++){
                Log.d(Constants.DEBUG_TAG, "channel[i] is: "+chNames.get(i));

                // TODO maybe try initialising arraylists here
                mSet = new LineDataSet(null, null);
                mDataSets = new ArrayList<>();
                mData = new LineData();

                List<Entry> vals = new ArrayList<Entry>();

                // TODO, currently can't select dateandtime
                for(int j=0; j<dataArray.length();j++){
                    JSONObject object = dataArray.getJSONObject(j);
                    String item = object.getString(chNames.get(i));

                    try {
                        // Convert value to float
                        Float fItem = Float.parseFloat(String.valueOf(item));

                        // Create an plot entry and add it to the entries list
                        Entry entry = new Entry(j, fItem);
                        mEntries.add(entry);


                        //Log.d(Constants.DEBUG_TAG,"entry :"+entry);
                    }catch (NumberFormatException e){
                        e.printStackTrace();

                        //TODO this needs to be a dialogue
                        Toast.makeText(getBaseContext(), R.string.cannot_plot, Toast.LENGTH_LONG).show();

                    }
                    //mList.add(fItem);
                }
                // Create LineDataSet Object (list, label)
                mSet = new LineDataSet(mEntries, chNames.get(i));
                mSet.setColor((Integer) mColours.get(i));
                mSet.setAxisDependency(YAxis.AxisDependency.LEFT);

                mDataSets = new ArrayList<ILineDataSet>();
                mDataSets.add(mSet);
                mData = new LineData(mDataSets);
                createLineChart(mData, i+1);
                Log.d(Constants.DEBUG_TAG,"LineData length "+ mData.getDataSetCount());

                // Empty the data to get next channel's data
                /*mSet.clear();
                mEntries.clear();
                data.clearValues();*/

              /*  // Create list of IDataSets
                LineData data = addDataLineSet(mSet);
                createLineChart(data,i+1);
                *//*mAllEntries.add(mEntries);
                mAllData.add(mList);
                mList.clear();*/
            }
            //mEntries.clear();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mAllEntries;
    }

    public LineData addDataLineSet(LineDataSet set){

        mDataSets.add(set);
        LineData data = new LineData(mDataSets);
        //Log.d(Constants.DEBUG_TAG, "Datasets length: "+mDataSets.size());
        return data;
    }

    public void createLineChart(LineData data, int chart){
        //LineData data = new LineData(dataSet);

        switch(chart){
            case 1:
                YAxis leftAxis = mLineChart1.getAxisLeft();

                //leftAxis.setAxisMaximum(100f);
                mLineChart1.setData(data);
                mLineChart1.invalidate();

                Log.d(Constants.DEBUG_TAG, "lineChart "+chart);
                break;
            case 2:
                Log.d(Constants.DEBUG_TAG, "Data for line chart :"+data);
                mLineChart2.setData(data);
                mLineChart2.invalidate();
                Log.d(Constants.DEBUG_TAG, "lineChart "+chart);

                break;
            case 3:
                mLineChart3.setData(data);
                mLineChart3.invalidate();
                Log.d(Constants.DEBUG_TAG, "lineChart "+chart);

                break;
            case 4:
                mLineChart4.setData(data);
                mLineChart4.invalidate();
                Log.d(Constants.DEBUG_TAG, "lineChart "+chart);

                break;
        }

    }


}
