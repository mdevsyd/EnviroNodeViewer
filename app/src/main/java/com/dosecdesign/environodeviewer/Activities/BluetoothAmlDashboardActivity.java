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
import android.view.View;
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

    private TextView mBattVTv, mBattTempTv, mSuppVTv, mCurrentTv, mSerialTv, mCommentTv, mNameTv, mWidget5Tv, mWidget6Tv, mWidget7Tv, mWidget8Tv, mWidget9Tv, mExtCurrentTv, mLivePlotTv, mDashTitleTv;
    private ImageView mDisconnectIv, mConnectIv, mBattVIv, mTempIv, mExtSuppVoltIv, mExtSuppCurrIv, mIntTempIv, mIntPressureIv, mExtCurrentIv, mWidget7Iv, mWidget8Iv, mWidget9Iv;


    private RealTimeDataRequest mReq;

    private LinearLayout mChartLayout, mWidgetsLayout;

    private LineChart mChart;
    private Boolean mLive = false, test = false;
    private byte[] mLiveReg;
    private int mChIndex;
    private float mVolt;
    private Handler mCurrentHandler;
    private Timer mCurrentTimer;
    private TimerTask mCurrentTimerTask;
    private String mRes;
    private Boolean mStartUp, mWidgets, mIsBtDevice;
    private int count=0;


    /**
     * Method to setup the view items and start the Bluetooth SPP Service.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_aml_dashboard);

        mBattVTv = (TextView) findViewById(R.id.battVTv);
        mBattTempTv = (TextView) findViewById(R.id.battTempTv);
        mSuppVTv = (TextView) findViewById(R.id.supplyVTv);
        mCurrentTv = (TextView) findViewById(R.id.supplyCurrentTv);
        mSerialTv = (TextView) findViewById(R.id.amlSerialTv);
        mCommentTv = (TextView) findViewById(R.id.unitCommentTv);
        mNameTv = (TextView) findViewById(R.id.unitNameTv);
        mWidget5Tv = (TextView) findViewById(R.id.widget5Tv);
        mWidget6Tv = (TextView) findViewById(R.id.widget6Tv);
        mWidget7Tv = (TextView) findViewById(R.id.widget7Tv);
        mWidget8Tv = (TextView) findViewById(R.id.widget8Tv);
        mWidget9Tv = (TextView) findViewById(R.id.widget9Tv);
        mExtCurrentTv = (TextView) findViewById(R.id.supplyCurrentTv);
        mLivePlotTv = (TextView) findViewById(R.id.livePlotTv);
        mDashTitleTv = (TextView) findViewById(R.id.dashTitleTv);



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
        mDisconnectIv = (ImageView) findViewById(R.id.disconnectIv);
        mDisconnectIv.setOnClickListener(this);
        mConnectIv = (ImageView) findViewById(R.id.connectIv);
        mConnectIv.setOnClickListener(this);
        mWidget7Iv = (ImageView)findViewById(R.id.widget7Iv);
        mWidget7Iv.setOnClickListener(this);
        mWidget8Iv = (ImageView)findViewById(R.id.widget8Iv);
        mWidget8Iv.setOnClickListener(this);
        mWidget9Iv = (ImageView)findViewById(R.id.widget9Iv);
        mWidget9Iv.setOnClickListener(this);


        mLiveDataType = "";
        mLiveReg = new byte[24];
        mChIndex = -1;
        mVolt = 0;
        mRes = "";
        mStartUp = true;
        mWidgets = false;

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


        mConnectIv.setVisibility(View.GONE);
        mDisconnectIv.setVisibility(View.GONE);


        // Get the device name and address from the intent that intiitated this activity
        Intent commsIntent = getIntent();
        mConnectedDeviceAddress = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
        //mConnectedDeviceName = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_NAME);

        startSPPService();

        mReq = new RealTimeDataRequest(mBTLoggerSPPService);
        hideWidgetAndChartArea();


    }

    /**
     * Responds to activity requests of connecting to a device
     * or enabling the bluetooth connection.
     * @param requestCode - int request code
     * @param resultCode -  int the requests result
     * @param data - the intent returned by the request
     */
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

    /**
     * Method that runs once the bluetoothe permissions request has been completed.
     * Only if the permission is granted, is a connection is established.
     * @param requestCode - int the type of request performed
     * @param permissions - array of permissions requested
     * @param grantResults -  array of results for requested permissions
     */
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
     * and update the UI based on returned messages.
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // switch on what the message contains to determine action
            switch (msg.what) {
                // the Bluetooth state has changed
                case Constants.MESSAGE_STATE_CHANGE:
                    // switch on arg1 --> this is the state of BluetoothChatService
                    switch (msg.arg1) {
                        // update UI to show connection status
                        case BtLoggerSPPService.STATE_CONNECTED:
                            // check if its first time opening view
                            if (mStartUp) {
                                mConnectIv.setVisibility(View.VISIBLE);
                                mDisconnectIv.setVisibility(View.VISIBLE);
                                startSPPService();
                                // display and update the widget values
                                showWidgetArea();
                                mDashTitleTv.setText(R.string.aml_connected);
                                mReq.requestUnitName();


                                // startup is complete
                                mStartUp = false;
                            } else {
                                mDashTitleTv.setText(R.string.aml_connected);
                            }
                            break;
                        case BtLoggerSPPService.STATE_CONNECTING:
                            mDashTitleTv.setText(R.string.aml_connecting);
                            break;
                        case BtLoggerSPPService.STATE_LISTEN:
                            // do nothing to UI, we are listening for a connection
                            break;
                        case BtLoggerSPPService.STATE_NONE:
                            mDashTitleTv.setText(R.string.aml_not_connected);

                            break;
                    }
                    break;

                // We are reading a returned Bluetooth message from  the CC2564
                case Constants.MESSAGE_READ:
                    // create byte array of the response
                    byte[] readBuffer = (byte[]) msg.obj;
                    // 'command' will determine what type of data is being returned
                    byte[] command = Arrays.copyOfRange(readBuffer, 0, 3);
                    String commandStr = new String(command);
                    // take action depending on first three bytes
                    switch (commandStr) {
                        // sf7 --> overall unit comment
                        case "sf7":
                            Log.d(Constants.DEBUG_TAG, "got name");
                            String unitName = new String(Arrays.copyOfRange(readBuffer, 3, 29));
                            mNameTv.setText(unitName);
                            mReq.requestUnitComment();
                            break;
                        // sd7 --> overall unit comment
                        case "sd7":
                            Log.d(Constants.DEBUG_TAG, "got comment");

                            String unitComment = new String(Arrays.copyOfRange(readBuffer, 3, 29));
                            mCommentTv.setText(unitComment);
                            mReq.requestSerialNumber();
                            break;
                        // al2 --> 8 bytes = overall unit serial number
                        case "al2":
                            Log.d(Constants.DEBUG_TAG, "got serial");

                            String serial = new String(Arrays.copyOfRange(readBuffer, 3, 11));
                            Log.d(Constants.DEBUG_TAG,"serial"+serial);
                            mSerialTv.setText(serial);
                            mReq.requestBattDetails();
                            break;
                        // aw2 -->  batt voltage and batt temperature
                        // 4 bytes = battV * 1000 then 4 bytes =  battTemp*10
                        case "aw2":
                            Log.d(Constants.DEBUG_TAG, "got batt details");

                            String battV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            String battT = new String(Arrays.copyOfRange(readBuffer, 7, 11));
                            try {
                                float battVolts = Float.valueOf(battV) / 1000;
                                float battTemp = Float.valueOf(battT) / 10;
                                // Check if we are in live or dashboard mode
                                if (mLiveReg[0] == 0) {
                                    // We are in dashboard mode
                                    mBattVTv.setText(String.valueOf(battVolts));
                                    mBattTempTv.setText(String.valueOf(battTemp));
                                } else if (mLiveReg[0] == 1) {
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
                            mReq.requestExternalSupply();
                            break;
                        // ay1 -->  external supply voltage*1000
                        case "ay2":
                            Log.d(Constants.DEBUG_TAG, "got external supp");

                            Log.d(Constants.DEBUG_TAG,"respond ext supp");
                            String extV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            String extC = new String(Arrays.copyOfRange(readBuffer, 7, 11));
                            try {
                                float extVolts = (Float.valueOf(extV) / 1000);
                                float extCurrent = (Float.valueOf(extC));
                                mVolt = extVolts;
                                mSuppVTv.setText(String.valueOf(extVolts));
                                mExtCurrentTv.setText(String.valueOf(extCurrent));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Log.e(Constants.ERROR_TAG, "Error parsing external supply voltage data to float in mHandler");
                                Toast.makeText(BluetoothAmlDashboardActivity.this, R.string.ext_data_error, Toast.LENGTH_SHORT).show();
                            }
                            getChannels();
                            break;
                        // rb9 --> channel data : [4 byte ch][block 1[4 byte  raw][4 byte converted]][block 2[4 byte raw][4 byte converted]]
                        //                                   [block 3[4 byte  raw][4 byte converted]][block 4[4 byte raw][4 byte converted]]
                        case "rb9":
                            Log.d(Constants.DEBUG_TAG, "got channels");
                            count++;

                            // extract converted values, save to byte arrays for each channel
                            // chan 1 start byte = 11, end byte = 11 + 4
                            byte[] chan1 = Arrays.copyOfRange(readBuffer, 11, 15);
                            // chan 2 start byte = 11 + 8, end byte = 11 + 8 + 4
                            byte[] chan2 = Arrays.copyOfRange(readBuffer, 19, 23);
                            // chan 3 start byte = 11 + 8 + 8, end byte = 11 + 8 + 8 + 4
                            byte[] chan3 = Arrays.copyOfRange(readBuffer, 27, 31);
                            // chan 4 start byte = 11 + 8 + 8 + 8, end byte = 11 + 8 + 8 + 8 + 4
                            byte[] chan4 = Arrays.copyOfRange(readBuffer, 35, 39);

                            // convert to floats --> plot requires Entry(float,float)
                            float chan1float = ByteBuffer.wrap(chan1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            float chan2float = ByteBuffer.wrap(chan2).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            float chan3float = ByteBuffer.wrap(chan3).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            float chan4float = ByteBuffer.wrap(chan4).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                            // check which mode we are in to update appropriate widgets
                            if (mLiveReg[0] == 0) {
                                // we are not live, update aml dashboard channel widgets
                                mWidget6Tv.setText(String.valueOf(chan1float));
                                mWidget5Tv.setText(String.valueOf(chan2float));
                                mWidget7Tv.setText(String.valueOf(chan3float));
                                mWidget8Tv.setText(String.valueOf(chan4float));
                                //mWidget9Tv.setText(String.valueOf(chan4float));
                            } else if (mLiveReg[0] == 1) {
                                // we are in live mode, set the title of the plot
                                mLivePlotTv.setText(mLiveDataType);
                                // check status reg to see which channel we want to plot
                                int ch = determineChanFromStatusReg();
                                if (ch >= 5 && ch <= 8) {
                                    switch (ch) {
                                        case 5:
                                            // internal temp (AML channel 1)
                                            addChartEntry(chan2float);
                                            break;
                                        case 6:
                                            // internal pressure (AML channel 2)
                                            addChartEntry(chan1float);
                                            break;
                                        case 7:
                                            // channel 3
                                            addChartEntry(chan3float);
                                            break;
                                        case 8:
                                            // channel 4
                                            addChartEntry(chan4float);
                                            break;
                                    }
                                }
                            }
                            mReq.cancelActiveHandlers();
                            //getChannels();
                            //mReq.requestUnitName();
                            if(count>2){
                                mReq.cancelActiveHandlers();
                                Log.d(Constants.DEBUG_TAG,"refreshing runnables - counter :"+count);

                                mReq.requestBattDetails();
                                count=0;
                            }
                            mReq.requestBattDetails();
                            break;
                    }

                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d(Constants.DEBUG_TAG, "toast msg from SPP" + msg.getData().getString(Constants.TOAST));
                    // we are unable to connect, alert user
                    if (mStartUp) {
                        mBTLoggerSPPService.stop();
                        mBTLoggerSPPService.start();
                        // connection failed due to user attempting to connect to non environmental logger BT device
                        showCustomDialog();
                        mStartUp = false;

                    } else {
                        // connection failed due to other reason, show toast
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Method checks the register to see which bit has been set.
     * @return the index of the channel with bit set.
     */
    private int determineChanFromStatusReg() {
        int ch = -1;
        for (int i = 5; i < mLiveReg.length; i++) {
            if (mLiveReg[i] == 1) {
                ch = i;
            }
        }
        return ch;
    }

    /**
     * Initiates bluetooth enabling if it is not enabled when activity is started.
     * Otherwise, starts the bluetooth SPP service.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the  session
        } else if (mBTLoggerSPPService == null) {
            mBTLoggerSPPService = new BtLoggerSPPService(this, mHandler);
            mBTLoggerSPPService.start();
        }
        mReq.cancelActiveHandlers();
    }

    /**
     * Stops the Bluetooth SPP service and cancels any active tasks when activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mReq.cancelActiveHandlers();
        if (mBTLoggerSPPService != null) {
            mBTLoggerSPPService.stop();
        }
        mLive = false;
    }

    /**
     * Stops any active handlers updating the UI when activity gets paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        mReq.cancelActiveHandlers();
    }

    /**
     * Stops any active handlers updating the UI when activity is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
        mReq.cancelActiveHandlers();

    }

    /**
     * Starts the SPP service if the activity was resumed - when the user navigates
     * away from the app and then returns or when the live chart is removed from the view.
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        mReq.cancelActiveHandlers();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTLoggerSPPService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTLoggerSPPService.getState() == BtLoggerSPPService.STATE_NONE) {
                //start SPP Service
                startSPPService();
            }
        } else {
            // there is no instance of the SPP service, create and start one
            mBTLoggerSPPService = new BtLoggerSPPService(this, mHandler);
            mBTLoggerSPPService.start();
        }
        // only update the dashboard if widgets should be viewed
        /*if (mWidgets) {
            updateBtDashboard();
        }*/
    }

    /**
     * Gets the current connection state of the SPP service
     * @return - SPP state
     */
    public int getConnectionState() {
        return mBTLoggerSPPService.getState();
    }

    /**
     *  Checks to make sure no instnace of SPP service exists before creating one and connecting the
     *  Bluetooth device.
     */
    public void startSPPService() {
        if (getConnectionState() == BtLoggerSPPService.STATE_NONE) {
            // There is no instance of the SPP service, start one.
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();

            // Get the BLuetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);

            // Connect to the BT device
            mBTLoggerSPPService.connect(device);
        }
    }

    /**
     * Define actions based on what the user has clicked within the activity
     * @param v -  the view id of view item click on by user
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            // The following case is for when user click on any of the widget images,
            case (R.id.battIv):
                mLiveDataType = "battery voltage";
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[1] = 1;
                // Hide widgets and display the graph
                hideWidgetArea();
                break;
            case (R.id.widget5Iv):
                // temp
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[5] = 1;
                mLiveDataType = "Internal Temperature Sensor (Deg. C)";
                hideWidgetArea();
                break;
            case (R.id.widget6Iv):
                //pressure
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[6] = 1;
                mLiveDataType = "Internal Pressure Sensor (mbar)";
                hideWidgetArea();
                break;
            case (R.id.widget7Iv):
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[7] = 1;
                mLiveDataType = "Volumetric Soil Moisture";
                hideWidgetArea();
                break;
            case (R.id.widget8Iv):
                // set all action reg to 0
                clearActionReg();
                // set required bits in action reg
                mLiveReg[0] = mLiveReg[8] = 1;
                mLiveDataType = "Volumetric Water Content";
                hideWidgetArea();
                break;
            case R.id.disconnectIv:
                disconnectDevice();
                hideWidgetAndChartArea();
                mReq.cancelActiveHandlers();

                break;
            case R.id.connectIv:
                startSPPService();
                showWidgetArea();
                break;


        }

    }

    /**
     * Stops the Bluetooth service and clears action register.
     */
    private void disconnectDevice() {
        mBTLoggerSPPService.stop();
        mReq.cancelActiveHandlers();
        clearActionReg();
    }

    /**
     * Method to setup the chart view and line data parameters, refresh chart.
     */
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
        mChart.invalidate();
    }

    /**
     * Method creates a data entry and adds it to DataSet to be plotted in LineChart.
     * Notifies LineData and chart of changes to the DataSet (once per second).
     * Refreshes the chart after receiving the latest data.
     *
     * @param dataFloat - the float to be plotted.
     */
    private void addChartEntry(float dataFloat) {

        // Get reference to the current data on the plot
        LineData data = mChart.getData();

        // If no data exists, create a data set
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

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
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setCubicIntensity(0.05f);
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

    /**
     * Displays the dashboard widget area on the UI,
     * removes live chart view
     */

    private void showWidgetArea() {
        mWidgets = true;
        mWidgetsLayout.setVisibility(View.VISIBLE);
        mChartLayout.setVisibility(View.GONE);
        // We are exiting live mode, set all bits in livemode register back to zero
        clearActionReg();
    }

    /**
     * Removes the dashboard widget area from the UI view,
     * replaces it with live chart view.
     */
    private void hideWidgetArea() {
        mWidgets = false;
        // We are going into live mode, set the live mode bit in reg
        mLiveReg[0] = 1;
        // clear previous chart data
        setupChart();
        // swap the visible widget area type
        mWidgetsLayout.setVisibility(View.GONE);
        mChartLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the dashboard widget area and chart view. Used for when no device is connected and activity has
     * just been started.
     */
    private void hideWidgetAndChartArea() {
        mWidgets = false;
        // user has clicked disconnect, hide both areas
        clearActionReg();
        mWidgetsLayout.setVisibility(View.GONE);
        mChartLayout.setVisibility(View.GONE);
        mNameTv.setText("--");
        mSerialTv.setText("--");
        mCommentTv.setText("--");
    }


    /**
     * Setup and display dialogue when device is not connectable. Present option to connect
     * to other deivce - yes returns user to device list, no takes back to ation select activity.
     */
    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.device_conn_fail);
        builder.setMessage("Try another device?");

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mBTLoggerSPPService.getState()!=BtLoggerSPPService.STATE_NONE){
                    mBTLoggerSPPService.stop();
                }
                Intent intent = new Intent(getApplicationContext(), DeviceListActivity.class);
                startActivity(intent);
                mReq.cancelActiveHandlers();
                mStartUp = true;

            }
        })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.d(Constants.DEBUG_TAG, "service: " + mBTLoggerSPPService.getState());
                        mBTLoggerSPPService.stop();
                        Log.d(Constants.DEBUG_TAG, "service after : " + mBTLoggerSPPService.getState());
                        Intent intent = new Intent(getApplicationContext(), OptionSelectActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                        mStartUp = false;
                    }
                });
        builder.create().show();

    }

    /**
     * Method to update channel widgets over Bluetooth classic. Creates a byte array
     * including command and channel index. Requests are made by using the RealTimeDataRequest
     * object. Responses are handled by mHandler.
     */
    private void getChannels() {
        // Setup the channel request byte array
        byte[] channelMsg = new byte[7];
        // 3 bytes command
        channelMsg[0] = 'R';
        channelMsg[1] = 'B';
        channelMsg[2] = '1';
        // 4 bytes requested channels starting with first channel index
        channelMsg[3] = 0;
        channelMsg[4] = 0;
        channelMsg[5] = 0;
        channelMsg[6] = 0;
        mChIndex = channelMsg[3];
        // request channel data
        mReq.requestChannelData(channelMsg);
    }

    /**
     * Method to handle back press events and only end the activity if user is not in live mode.
     */
    @Override
    public void onBackPressed() {
        // Check if any of the register bits are set, if so, we were in live mode,
        // so pressing back should not end the activity
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
        for (int i = 0; i < mLiveReg.length; i++) {
            mLiveReg[i] = 0;
        }
    }


}
