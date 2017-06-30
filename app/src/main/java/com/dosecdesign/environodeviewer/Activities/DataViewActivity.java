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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for plotting the returned channel data from server.
 */
public class DataViewActivity extends AppCompatActivity {

    private String mCachedResponse, mHubName, mInstName, mChName;

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

    private TextView mPlotTitleTv, mHubTv, mInstTv  ;

    /**
     * Setup the activity views, obtain the cached response from the query that takes us to this
     * activity.
     * @param savedInstanceState - the bundle holding contents passed to this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        mLineChart1 = (LineChart) findViewById(R.id.dataPlot1);
        mPlotTitleTv = (TextView) findViewById(R.id.plotTitleTv);

        mDevMem = new DeviceMemoryUtils();

        // Read from the cache
        mCachedResponse = mDevMem.readFromCache(getCacheDir(), "channels");

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
        mChName = extras.getString(Constants.SELECTED_CHANNEL_NAME);
        mHubName = extras.getString(Constants.SELECTED_HUB);
        mInstName = extras.getString(Constants.SELECTED_INSTRUMENT);

        mHubTv = (TextView) findViewById(R.id.hubTv);
        mInstTv = (TextView) findViewById(R.id.instTv);

        getValuesFromResponse(extras.getInt(Constants.SELECTED_CHANNELS), mSelChannels);
    }

    /**
     * Create entries for line chart. Creates a dataset and lineData
     * with Y values only.
     *
     * @param numCh   amount of channels selected by user
     * @param chNames names of channels selected by user
     * @return arraylist of entry values for plotting
     */
    public ArrayList<Entry> getValuesFromResponse(int numCh, ArrayList<String> chNames) {
        try {
            // Get channel data to plot from cached file
            JSONObject responseObj = new JSONObject(mCachedResponse);
            // get reference to data array
            JSONArray dataArray = responseObj.getJSONArray("data");
            // iterate through amount of channels selected by user
            for (int i = 0; i < numCh; i++) {
                // set the plot textviews
                mPlotTitleTv.setText(chNames.get(i));
                mHubTv.setText("Hub "+mHubName);
                mInstTv.setText("Device "+mInstName);
                // iterate through amount of data points in array the values
                for (int j = 0; j < dataArray.length(); j++) {
                    // get reference to object (Channel Name: int value)
                    JSONObject object = dataArray.getJSONObject(j);
                    // get YAxis value
                    String item = object.getString(chNames.get(i));
                    try {
                        // Convert value to float --> Entry(float, float)
                        Float fItem = Float.parseFloat(String.valueOf(item));
                        // Create an plot entry and add it to the entries list with dummy X value
                        Entry entry = new Entry(j, fItem);
                        mEntries.add(entry);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), R.string.cannot_plot, Toast.LENGTH_LONG).show();
                    }
                }
                // create the data set from the values in mEntries, set label
                mSet = new LineDataSet(mEntries, chNames.get(i));
                // setup the dataset visual characteristics
                mSet.setColor(R.color.colorPrimary);
                mSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                mSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                mSet.setDrawFilled(true);
                mSet.setFillColor(Color.CYAN);
                // create arraylist and assign the set of data
                mDataSets = new ArrayList<>();
                mDataSets.add(mSet);
                mData = new LineData(mDataSets);
                // add and format datestamps onto XAxis
                setupXValues();
                // setup nd refresh chart
                createLineChart(mData);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mAllEntries;
    }

    /**
     * Method to set chart characteristics and refresh chart.
     * @param data
     */
    public void createLineChart(LineData data) {
        mLineChart1.setPinchZoom(true);
        mLineChart1.setDescription(null);
        mLineChart1.animateX(2000);
        mLineChart1.setData(data);
        mLineChart1.invalidate();
    }

    /**
     * Method to setup the XAxis style and format the XAxis values
     * using value formatter.
     */
    private void setupXValues() {
        try {
            //get reference to the X axis and set its position on chart
            XAxis xAxis = mLineChart1.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            // setup visual X Axis characteristics
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            // ensure first and last XAxis value can be entirely seen
            xAxis.setAvoidFirstLastClipping(true);
            // use value formatter to add datestamps to XAxis
            xAxis.setValueFormatter(new MyAxisValueFormatter(mDatestamps));

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(Constants.ERROR_TAG, "Trying to set axis values on null " +
                    "chart in DataViewActivity");
        }
    }


    /**
     * Formatter used to return the value stored in array of dates to be
     * displayed on the XAxis of chart
     */
    public class MyAxisValueFormatter implements IAxisValueFormatter {

        private String[] mXVals;

        public MyAxisValueFormatter(String[] xVals) {
            this.mXVals = xVals;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // value is the position of the label on the x axis
            return mXVals[(int) value];
        }

    }


}
