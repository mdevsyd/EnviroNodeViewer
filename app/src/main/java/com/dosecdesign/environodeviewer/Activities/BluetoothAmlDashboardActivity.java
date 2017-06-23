package com.dosecdesign.environodeviewer.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.Utitilies.RealTimeDataRequest;
import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Services.BtLoggerSPPService;
import com.dosecdesign.environodeviewer.Utitilies.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity populates views with AML data acquired over Bluetooth connection with
 * the Bluetooth external logger.
 */

public class BluetoothAmlDashboardActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceAddress = null;
    private String mLiveDataType;

    private BtLoggerSPPService mBTLoggerSPPService = null;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private TextView mBattVTv, mBattTempTv, mSuppVTv, mSerialTv, mCommentTv, mNameTv, mwidget5Tv, mwidget6Tv, mExtCurrentTv, mLivePlotTv;
    private ImageView mBattVIv, mTempIv, mExtSuppVoltIv, mIntTempIv, mIntPressureIv, mExtCurrentIv;

    private Button mTestBtn, mTestBtn2;
    private ImageButton mRefreshBtn;
    private TimerTask mBatteryTask;
    private Handler mSecHandler, mBatteryHandler;
    private Timer mBattTimer, mCommentTimer, mExternalTimer, mSerialTimer, mNameTimer;
    private TimerTask mExternalTimerTask, mCommentTimerTask, mSerialTimerTask, mNameTimerTask;

    private RealTimeDataRequest mReq;

    private LinearLayout mChartLayout, mWidgetsLayout;

    private LineChart mChart;
    private Boolean mLive = false, test = false;
    private byte[] mLiveReg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_aml_dashboard);

        mBattVTv = (TextView) findViewById(R.id.battVTv);
        mBattTempTv = (TextView) findViewById(R.id.battTempTv);
        mSuppVTv = (TextView) findViewById(R.id.supplyVTv);
        mSerialTv = (TextView) findViewById(R.id.amlSerialTv);
        mCommentTv = (TextView) findViewById(R.id.unitCommentTv);
        mNameTv = (TextView) findViewById(R.id.unitNameTv);
        mwidget5Tv = (TextView) findViewById(R.id.widget5Tv);
        mwidget6Tv = (TextView) findViewById(R.id.widget6Tv);
        mExtCurrentTv = (TextView) findViewById(R.id.supplyCurrentTv);
        mLivePlotTv = (TextView) findViewById(R.id.livePlotTv);


        mBattVIv = (ImageView) findViewById(R.id.battIv);
        mBattVIv.setOnClickListener(this);
        mTempIv = (ImageView) findViewById(R.id.battTempIv);
        mTempIv.setOnClickListener(this);
        mExtSuppVoltIv = (ImageView) findViewById(R.id.solarIv);
        mExtSuppVoltIv.setOnClickListener(this);
        mIntTempIv = (ImageView) findViewById(R.id.widget5Iv);
        mIntTempIv.setOnClickListener(this);
        mIntPressureIv = (ImageView) findViewById(R.id.widget6Iv);
        mIntPressureIv.setOnClickListener(this);
        mExtCurrentIv = (ImageView) findViewById(R.id.supplyCurrentIv);
        mExtCurrentIv.setOnClickListener(this);

        mRefreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
        mRefreshBtn.setOnClickListener(this);
        mTestBtn = (Button) findViewById(R.id.testBtn);
        mTestBtn.setOnClickListener(this);

        mTestBtn2 = (Button) findViewById(R.id.testBtn2);
        mTestBtn2.setOnClickListener(this);

        mLiveDataType = "";
        mLiveReg = new byte[8];

        mWidgetsLayout = (LinearLayout) findViewById(R.id.widgetArea);

        mChartLayout = (LinearLayout) findViewById(R.id.liveChartLayout);
        mChartLayout.setVisibility(View.GONE);

        mChart = (LineChart) findViewById(R.id.liveDataPlot);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(BluetoothAmlDashboardActivity.this, R.string.bt_unaavailble, Toast.LENGTH_LONG).show();
            finish();
        }

        // Create a local instance of SPPService
        mBTLoggerSPPService = new BtLoggerSPPService(this, mHandler);


        // Get the device name and address from the intent that intiitated this activity
        Intent commsIntent = getIntent();
        mConnectedDeviceAddress = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
        //mConnectedDeviceName = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_NAME);

        mReq = new RealTimeDataRequest(mBTLoggerSPPService);
        updateBtDashboard();


    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case Constants.REQUEST_CONNECT_DEVICE:

                // When DeviceListActivity returns with a device to connect
                if (resultCode == RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(Constants.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mConnectedDeviceAddress = address;

                    // Check for Bluetooth permissions
                    int hasBluetoothPermissions = ContextCompat.checkSelfPermission(BluetoothAmlDashboardActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (hasBluetoothPermissions != PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(BluetoothAmlDashboardActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            // add message to allow access to BT

                        }
                        return;
                    }
                    ActivityCompat.requestPermissions(BluetoothAmlDashboardActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            Constants.REQUEST_CODE_ASK_BT_PERMISSIONS);
                    return;

                }

                break;

            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != RESULT_OK) {
                    Log.d(Constants.DEBUG_TAG, "BT not enabled");
                    finish();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Constants.REQUEST_CODE_ASK_BT_PERMISSIONS:
                // If the request gets cancelled, result[] is empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Attempt to connect to the device
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);
                    mBTLoggerSPPService.connect(device);
                }
        }
    }

    /**
     * Handler to handle messages back from the BluetoothSPP Service
     */
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    // Update the status by switching on arg1 --> state of BluetoothChatService
                    switch (msg.arg1) {
                        case BtLoggerSPPService.STATE_CONNECTED:
                            // update UI to show connected
                            setStatus(getString(R.string.title_connected));
                            break;
                        case BtLoggerSPPService.STATE_CONNECTING:
                            // update UI to show connecting
                            setStatus(getString(R.string.title_connecting));
                            break;
                        case BtLoggerSPPService.STATE_LISTEN:
                            // do nothing to UI, we are listening for a connection
                            break;
                        case BtLoggerSPPService.STATE_NONE:
                            // update UI, we are currently not connected
                            setStatus(getString(R.string.title_disconnected));

                            break;
                    }
                    break;

                // We are reading a returned Bluetooth message from the CC2564
                case Constants.MESSAGE_READ:
                    // create byte array of the response
                    byte[] readBuffer = (byte[]) msg.obj;
                    // 'command' will determine what type of data is being returned
                    byte[] command = Arrays.copyOfRange(readBuffer, 0, 3);
                    String commandStr = new String(command);
                    // take action depending on first three bytes of command
                    switch (commandStr) {
                        // sf7 --> overall unit comment
                        case "sf7":
                            String unitName = new String(Arrays.copyOfRange(readBuffer, 3, 29));
                            mNameTv.setText(unitName);
                            break;
                        // sd7 --> overall unit comment
                        case "sd7":
                            String unitComment = new String(Arrays.copyOfRange(readBuffer, 3, 29));
                            mCommentTv.setText(unitComment);
                            break;
                        // al2 --> 8 bytes = overall unit serial number
                        case "al2":
                            String serial = new String(Arrays.copyOfRange(readBuffer, 3, 11));
                            mSerialTv.setText(serial);
                            break;
                        // aw2 -->  batt voltage and batt temperature
                        // 4 bytes = battV * 1000 then 4 bytes =  battTemp*10
                        case "aw2":
                            String battV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            String battT = new String(Arrays.copyOfRange(readBuffer, 7, 11));
                            try {
                                float battVolts = Float.valueOf(battV) / 1000;
                                float battTemp = Float.valueOf(battT) / 10;
                                // Check if we are in live or dashboard mode

                                if (mLiveReg[0]==0) {
                                    // We are in dashboard mode
                                    mBattVTv.setText(String.valueOf(battVolts));
                                    mBattTempTv.setText(String.valueOf(battTemp));
                                }
                                else if (mLiveReg[0]==1) {
                                    // We are in live mode
                                    if (mLiveReg[1] == 1) {
                                        // live batt mode --> We will be receiving data once per second, add a chart entry each rx
                                        addChartEntry(battVolts);
                                    }
                                }

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Log.e(Constants.ERROR_TAG, "Error parsing batt data to float in mHandler");
                                Toast.makeText(BluetoothAmlDashboardActivity.this, R.string.batt_data_error, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        // ay1 -->  external supply voltage*1000
                        case "ay1":
                            String extV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            try {
                                float extVolts = (Float.valueOf(extV) / 1000);
                                mSuppVTv.setText(String.valueOf(extVolts));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Log.e(Constants.ERROR_TAG, "Error parsing external supply voltage data to float in mHandler");
                                Toast.makeText(BluetoothAmlDashboardActivity.this, R.string.ext_data_error, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        // rb9 --> channel data : [4 byte ch][block 1[4 byte  raw][4 byte converted]][block 2[4 byte raw][4 byte converted]]
                        //                                   [block 3[4 byte  raw][4 byte converted]][block 4[4 byte raw][4 byte converted]]
                        case "rb9":
                            // extract converted values, save to byte arrays for each channel
                            // chan 1 start byte = 11, end byte = 11 + 4
                            byte[] chan1 = Arrays.copyOfRange(readBuffer, 11, 15);
                            // chan 2 start byte = 11 + 8, end byte = 11 + 8 + 4
                            byte[] chan2 = Arrays.copyOfRange(readBuffer, 19, 23);
                            // chan 3 start byte = 11 + 8 + 8, end byte = 11 + 8 + 8 + 4
                            byte[] chan3 = Arrays.copyOfRange(readBuffer, 27, 31);
                            // chan 4 start byte = 11 + 8 + 8 + 8, end byte = 11 + 8 + 8 + 8 + 4
                            byte[] chan4 = Arrays.copyOfRange(readBuffer, 35, 39);

                            float chan1float = ByteBuffer.wrap(chan1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            float chan2float = ByteBuffer.wrap(chan2).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            float chan3float = ByteBuffer.wrap(chan1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            float chan4float = ByteBuffer.wrap(chan2).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                            // check which mode we are in to update appropriate widget
                            if (mLiveReg[0] == 0) {
                                // we are not live, update aml dashboard channel widgets
                                mwidget6Tv.setText(String.valueOf(chan1float));
                                mwidget5Tv.setText(String.valueOf(chan2float));
                            } else if (mLiveReg[0] == 1) {
                                // we are live, set the title of the graph
                                mLivePlotTv.setText(mLiveDataType);
                                // check action register and initiate live plotting

                                // take away
                                float x = chan3float;
                                float y = chan4float;
                                Log.d(Constants.DEBUG_TAG, "flaot 1 and 2 " + String.valueOf(chan1float) + " " + String.valueOf(chan2float));


                                if (mLiveReg[5] == 1) {
                                    addChartEntry(chan2float);
                                } else if (mLiveReg[6] == 1) {
                                    addChartEntry(chan2float);
                                }

                            }
                            break;
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBTLoggerSPPService == null) {
            mBTLoggerSPPService = new BtLoggerSPPService(this, mHandler);
        }
        mReq.cancelActiveTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReq.cancelActiveTimers();
        if (mBTLoggerSPPService != null) {
            mBTLoggerSPPService.stop();
        }
        mLive = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReq.cancelActiveTimers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mReq.cancelActiveTimers();

    }

    @Override
    public void onResume() {
        super.onResume();
        mReq.cancelActiveTimers();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTLoggerSPPService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTLoggerSPPService.getState() == BtLoggerSPPService.STATE_NONE) {

                //start SPP Service
                startSPPService();
            }
        }
        updateBtDashboard();

    }

    public int getConnectionState() {
        return mBTLoggerSPPService.getState();
    }

    public void startSPPService() {
        if (getConnectionState() == BtLoggerSPPService.STATE_NONE) {
            // There is no instance of the SPP service, start one.
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();


            // Get the BLuetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);

            // Connect to the BT device
            mBTLoggerSPPService.connect(device);
        } else if (getConnectionState() == BtLoggerSPPService.STATE_CONNECTED) {
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.testBtn:
                // TODO remove this
                hideWidgetArea();
                break;
            case R.id.testBtn2:
                // TODO remove
                showWidgetArea();
                //mReq.getOnceOffBattV();
                test = true;
                //mBTLoggerSPPService.write("AW0".getBytes());
                // SPP service is active, get live data
                break;
            case R.id.refreshBtn:
                mReq.cancelActiveTimers();
                updateBtDashboard();
                break;
            // The following case is for when user click on any of the widget images,
            // they will be prompted if they'd like to go into live mode.
            case (R.id.battIv):
                mLiveDataType = "battery voltage";
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[1] =1;
                // Hide widgets and display the graph
                hideWidgetArea();
                break;
            case (R.id.widget5Iv):
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[5] =1;
                mLiveDataType = "Internal Temperature Sensor (Deg. C)";
                hideWidgetArea();
        }

    }

    private void setupChart() {

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

    /**
     * Method creates a data entry and adds it to DataSet to be plotted in LineChart.
     * Notifies LineData and chart of changes to the DataSet (once per second).
     * Refreshes the chart after receiving the latest data.
     *
     * @param dataFloat - the float to be plotted.
     */
    private void addChartEntry(float dataFloat) {

        Log.d(Constants.DEBUG_TAG, "data float: "+dataFloat);
        // Get reference to the current data on the plot
        LineData data = mChart.getData();

        // If no data exists, create a data set
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            Log.d(Constants.DEBUG_TAG, "creating set data");
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // Add an entry to the LineData set and notify of change
            data.addEntry(new Entry(set.getEntryCount(), dataFloat), 0);
            data.notifyDataChanged();

            // Notify the chart the data has changed and update chart
            mChart.notifyDataSetChanged();
            mChart.invalidate();

            // Setup view size and always stick to latest data result
            mChart.setVisibleXRangeMaximum(10);
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    /**
     * Method creates a set of data to be plotted and applies visual characteristics
     * to the line data set.
     *
     * @return - the ILineDataSet to be plotted.
     */
    private ILineDataSet createSet() {
        // Create a set of data, set label to the type of data selected by user
        LineDataSet set = new LineDataSet(null, mLiveDataType);

        // Setup the look of the line data on plot
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


    private void showWidgetArea() {
        mWidgetsLayout.setVisibility(View.VISIBLE);
        mChartLayout.setVisibility(View.GONE);
        // We are exiting live mode, set all bits in livemode register back to zero
        for (int i = 0; i < mLiveReg.length; i++) {
            mLiveReg[i] = 0;
        }
        //mLive=false;
    }

    private void hideWidgetArea() {
        // We are going into live mode, set the live mode bit in reg
        mLiveReg[0] = 1;
        setupChart();
        mWidgetsLayout.setVisibility(View.GONE);
        mChartLayout.setVisibility(View.VISIBLE);
    }


    /**
     * Setup and display dialogue asking user if they want to view live mode.
     */
    private void showCustomDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle(R.string.live_dialogue_title);
        builder.setMessage("Would you like to view live " + type + " data now?");

        builder.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                // Create an intent to start the api activity when user hits "GO". Send mKey in intent.
                Intent liveIntent = new Intent(BluetoothAmlDashboardActivity.this, LiveChartActivity.class);
                liveIntent.putExtra(Constants.LIVE_DATA_TYPE, mLiveDataType);
                liveIntent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, mConnectedDeviceAddress);
                startActivity(liveIntent);
                mReq.cancelActiveTimers();


            }
        })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();

    }

    /**
     * Method to update AML Dashboard widgets over Bluetooth classic.
     * Requests are made using the RealTimeDataRequest object.
     * Responses are handled by mHandler.
     */
    public void updateBtDashboard() {

        mReq.requestBattDetails();
        mReq.requestExternalSupply();
        mReq.requestSerialNumber();
        mReq.requestUnitComment();
        mReq.requestUnitName();

        // Setup the channel request byte array
        byte[] channelMsg = new byte[7];
        channelMsg[0] = 'R';
        channelMsg[1] = 'B';
        channelMsg[2] = '1';
        // now channel info...
        channelMsg[3] = 0; // channelIndex; // starting channel number
        channelMsg[4] = 0;
        channelMsg[5] = 0;
        channelMsg[6] = 0;
        mReq.requestLiveChannelData(channelMsg);
    }

    @Override
    public void onBackPressed() {
        // Check if any of the register bits are set, if so, we were live plotting, so
        // pressing back should not end the activity
        for (int i = 0; i < mLiveReg.length; i++) {
            if (mLiveReg[i] == 1) {
                showWidgetArea();
                // clear all existing chart data, setup fresh chart
                setupChart();
                clearActionReg();
                return;
            }
        }
        // we were not in live mode, end the activity on back press
        super.onBackPressed();
    }

    private void clearActionReg() {
        for (int i=0;i<mLiveReg.length;i++){
            mLiveReg[i]=0;
        }
    }

    public void setStatus(String status) {

    }

}
