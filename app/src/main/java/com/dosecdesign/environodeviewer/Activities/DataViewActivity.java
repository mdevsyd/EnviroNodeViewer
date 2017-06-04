package com.dosecdesign.environodeviewer.Activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.dosecdesign.environodeviewer.Utitilies.DeviceMemoryUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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
    private String[] mDatestamps;

    private DeviceMemoryUtils mDevMem;

    private TextView mPlot1Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        mLineChart1 = (LineChart) findViewById(R.id.dataPlot1);
        mLineChart2 = (LineChart) findViewById(R.id.dataPlot2);
        mLineChart3 = (LineChart) findViewById(R.id.dataPlot3);
        mLineChart4 = (LineChart) findViewById(R.id.dataPlot4);

        mPlot1Tv = (TextView)findViewById(R.id.plot1Tv);

        mDevMem = new DeviceMemoryUtils();

        // Read from the cache
        mCachedResponse = mDevMem.readFromCache(getCacheDir(),"channels");

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

        // Extract data from intent
        Bundle extras = getIntent().getExtras();
        mSelChannels = extras.getStringArrayList(Constants.SEL_CH_ARRAY);
        mDatestamps = extras.getStringArray(Constants.DATESTAMP_ARRAY);
        for (int i=0;i<mDatestamps.length;i++){
            Log.d(Constants.DEBUG_TAG, "mDatestamp: "+mDatestamps[i]);
        }

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
                mPlot1Tv.setText(chNames.get(i));


                // TODO maybe try initialising arraylists here
                mSet = new LineDataSet(null, null);
                mDataSets = new ArrayList<>();
                mData = new LineData();

                List<Entry> vals = new ArrayList<Entry>();

                // TODO, currently can't select dateandtime
                for(int j=0; j<dataArray.length();j++){

                    // Populate the string array for XAxis values


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
                mSet.setColor(R.color.colorPrimary);
                mSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                mSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                mSet.setDrawFilled(true);
                mSet.setFillColor(Color.CYAN);


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
        switch(chart){
            case 1:
                mLineChart1.setPinchZoom(true);
                mLineChart1.setDescription(null);
                mLineChart1.animateX(2000);
                mLineChart1.setData(data);
                mLineChart1.invalidate();

                try{
                    final String[] xVals = new String[mEntries.size()];
                    for (int i = 0; i < mEntries.size(); i++) {
                        xVals[i] = (""+mDatestamps[i]);
                    }
                    XAxis xAxis = mLineChart1.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(true);
                    xAxis.setGranularity(1f);
                    xAxis.setAvoidFirstLastClipping(true);
                    xAxis.setDrawGridLines(false);
                    xAxis.setValueFormatter(new MyAxisValueFormatter(xVals));

                }catch(NullPointerException e ){
                    e.printStackTrace();
                }

                break;
            case 2:
                mLineChart2.setData(data);
                mLineChart2.invalidate();

                break;
            case 3:
                mLineChart3.setData(data);
                mLineChart3.invalidate();

                break;
            case 4:
                mLineChart4.setData(data);
                mLineChart4.invalidate();

                break;
        }

    }


    public class MyAxisValueFormatter implements IAxisValueFormatter{

        private String[] mXVals;

        public MyAxisValueFormatter(String[] xVals){
            this.mXVals=xVals;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // value is the position of the label on the x axis
            return mXVals[(int)value];
        }

    }


}
