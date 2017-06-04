package com.dosecdesign.environodeviewer.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dosecdesign.environodeviewer.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


import java.util.Timer;
import java.util.TimerTask;

public class LiveChartActivity extends AppCompatActivity {

    private LineChart mChart;
    private TextView mChartTitle;
    private RelativeLayout mMainLayout;
    private Timer mSecTimer;
    private TimerTask mSecTimerTask;
    private Handler mSecHandler;
    private int mTempCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_chart);

        mMainLayout = (RelativeLayout) findViewById(R.id.activity_live_chart);
        mChartTitle = (TextView) findViewById(R.id.livePlotTv);

        mChart = new LineChart(this);
        mMainLayout.addView(mChart,new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT));

        // Customising the chart
        mChart.setDescription(null);
        mChart.setNoDataText("Acquiring Data...");

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        //Setup the linedata
        LineData data = new LineData();

        // Add data to the chart
        mChart.setData(data);

        // Get and customise legend
        Legend legend = mChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLUE);


        XAxis x = mChart.getXAxis();
        x.setTextColor(Color.BLUE);
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(true);

        YAxis y = mChart.getAxisLeft();
        y.setTextColor(Color.BLUE);
        y.setDrawGridLines(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Here we get the data once per second
        mSecHandler = new Handler();
        mSecTimer = new Timer();
        mSecTimerTask = new TimerTask() {
            @Override
            public void run() {
                mSecHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addEntry();
                    }
                });
            }
        };
        mSecTimer.schedule(mSecTimerTask,0,1000);


    }

    private void addEntry() {


        LineData data = mChart.getData();


        //TODO check this line
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            Float tempFloat = (float) Math.random() * 75;

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
/*
            Entry entry = new Entry(mTempCount, tempFloat);
            data.addEntry(entry, mTempCount);
            Log.d(Constants.DEBUG_TAG, "Adding an entry addEntry() - "+entry);*/

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            data.notifyDataChanged();
            mTempCount++;

            // notify data changed
            mChart.notifyDataSetChanged();
            mChart.invalidate();

            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mSecTimer.cancel();
        mSecTimer.purge();
        mSecTimerTask.cancel();
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Live Battery Voltage");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.RED);
        set.setValueTextColor(Color.BLUE);
        set.setValueTextSize(10f);

        return set;
    }
}
