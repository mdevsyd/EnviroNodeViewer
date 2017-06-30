package com.dosecdesign.environodeviewer.Utitilies;

import android.os.Handler;
import android.util.Log;

import com.dosecdesign.environodeviewer.Services.BtLoggerSPPService;

import java.util.Calendar;

/**
 * Class makes real time requests to CC2564 on AML.
 * Response is handled in calling activity's response handler.
 */

public class RealTimeDataRequest {

    private BtLoggerSPPService service;
    private Handler mBattHandler, mNameHandler, mCommentHandler, mSerialHandler, mExtHandler, mChannelHandler, mRefreshHandler, mMsgTimerHandler;
    private byte[] mMsg;
    boolean gotMsg = false;



    public RealTimeDataRequest(BtLoggerSPPService service){
        this.service=service;
    }

    public void startMsgTimer(){
        mMsgTimerHandler = new Handler();
        mMsgTimerHandler.postDelayed( timerRun, 2000);
    }

    public Boolean gotMessage(){
        return gotMsg;
    }

    public void setGotMsg(Boolean result){
        this.gotMsg = result;
    }

    private Runnable timerRun = new Runnable() {
        @Override
        public void run() {
            //service.write("SF0".getBytes());
        }
    };

    /**
     * Request the overall unit name set by user after 1 second delay.
     */
    public void requestUnitName() {
        Log.d(Constants.DEBUG_TAG,"req name");

        mNameHandler = new Handler();
        mNameHandler.postDelayed(nameRun, 1000);

        Log.d(Constants.DEBUG_TAG, "name handler done");

    }

    private Runnable nameRun = new Runnable() {
        @Override
        public void run() {
            service.write("SF0".getBytes());
        }
    };

    /**
     * Request the overall unit comment set by user after 1.05 sec delay.
     */
    public void requestUnitComment() {

        mCommentHandler =new Handler();
        mCommentHandler.postDelayed(commentRun, 125);
    }
    private Runnable commentRun = new Runnable() {
        @Override
        public void run() {
            service.write("SD0".getBytes());
        }
    };

    /**
     * Request serial number from CC2564 after 1.1 second delay.
     */
    public void requestSerialNumber() {
        Log.d(Constants.DEBUG_TAG,"serial req");
        mSerialHandler = new Handler();
        mSerialHandler.postDelayed(serialRun, 150);
    }
    private Runnable serialRun = new Runnable() {
        @Override
        public void run() {
            service.write("AL0".getBytes());
        }
    };

    /**
     * Request external supply details from CC2564 after 600ms delay
     * once a second.
     */
    public void requestExternalSupply() {
        Log.d(Constants.DEBUG_TAG,"req ext supply");
        mExtHandler = new Handler();
        mExtHandler.postDelayed(extRun, 200);

    }
    private Runnable extRun = new Runnable() {
        @Override
        public void run() {
            service.write("AY0".getBytes());
            //mExtHandler.postDelayed(this,3000);
        }
    };

    /**
     * Request battery voltage and temperature from CC2564 after 500ms delay
     * once a second.
     */

    public void requestBattDetails() {
        mBattHandler = new Handler();
        mBattHandler.postDelayed(battRun, 500);
    }
    // runnable to execute every ten seconds
    private Runnable battRun = new Runnable() {
        @Override
        public void run() {
            service.write("AW0".getBytes());
           // mBattHandler.postDelayed(this,10000);
        }
    };

    /**
     * Requests channel data from device once a second based on msg generated
     * in dashboard activity.
     * @param msg - byte array length 7 containing the command and channel index
     */
    public void requestChannelData(final byte[] msg) {
        mMsg=msg;
        // handler for timer task
        mChannelHandler = new Handler();
        // execute the runnable after 400ms
        mChannelHandler.postDelayed(chanRun, 400);

    }

    /**
     * Writes a message to CC2564 once every 2 seconds
     */
    private Runnable chanRun = new Runnable() {
        @Override
        public void run() {
            // send the channel request byte[] to CC2564
            service.write(mMsg);
            // repeat every 2 seconds
            //mChannelHandler.postDelayed(this,1000);
        }
    };


    public void refreshTasks(){
        mRefreshHandler = new Handler();
        mRefreshHandler.postDelayed(refreshRun, 100);
    }

    private Runnable refreshRun = new Runnable() {
        @Override
        public void run() {
            Log.d(Constants.DEBUG_TAG,"refreshing runnables "+ Calendar.getInstance().getTime());
            cancelActiveHandlers();
            requestUnitName();
            /*requestUnitComment();
            requestSerialNumber();
            requestBattDetails();
            requestExternalSupply();
            requestChannelData(mMsg);*/
            mRefreshHandler.postDelayed(this,120000);
        }
    };

    /**
     * Cancels any active timers and timerTasks, to eliminate
     * unneeded processes.
     */
    public void cancelActiveHandlers() {

        if(mBattHandler!=null){
            mBattHandler.removeCallbacks(battRun);
        }
        if(mNameHandler!=null){
            mNameHandler.removeCallbacks(nameRun);

        }
        if(mCommentHandler!=null) {
            mCommentHandler.removeCallbacks(commentRun);
        }
        if(mSerialHandler!=null) {
            mSerialHandler.removeCallbacks(serialRun);
        }
        if(mExtHandler!=null) {
            mExtHandler.removeCallbacks(extRun);
        }
        if(mChannelHandler!=null) {
            mChannelHandler.removeCallbacks(chanRun);
        }

    }
}
