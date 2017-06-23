package com.dosecdesign.environodeviewer.Utitilies;

import android.os.Handler;
import android.util.Log;

import com.dosecdesign.environodeviewer.Services.BtLoggerSPPService;
import com.dosecdesign.environodeviewer.Utitilies.Constants;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class makes real time requests to CC2564 on AML.
 * Response is handled in calling activity's response handler.
 */

public class RealTimeDataRequest {

    private BtLoggerSPPService service;
    private Handler mBattHandler, mNameHandler, mCommentHandler, mSerialHandler, mExtHandler, mChannelHandler;
    private Timer mBattTimer, mCommentTimer, mExternalTimer, mSerialTimer, mNameTimer, mChannelTimer ;
    private TimerTask mExternalTimerTask, mCommentTimerTask, mSerialTimerTask, mNameTimerTask, mBattTimerTask=null, mChTimerTask;


    public RealTimeDataRequest(BtLoggerSPPService service){
        this.service=service;
    }

    /**
     * Request the overall unit name set by user after 1 second delay.
     */
    public void requestUnitName() {
        mNameHandler = new Handler();
        mNameTimer = new Timer();
        mNameTimerTask = new TimerTask() {
            @Override
            public void run() {
                mNameHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        service.write("SF0".getBytes());
                    }
                });
            }
        };
        mNameTimer.schedule(mNameTimerTask, 400, 1000);
    }

    /**
     * Request the overall unit comment set by user after 1.05 sec delay.
     */
    public void requestUnitComment() {

        mCommentHandler = new Handler();
        mCommentTimer = new Timer();
        mCommentTimerTask = new TimerTask() {
            @Override
            public void run() {
                mCommentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        service.write("SD0".getBytes());
                    }
                });
            }
        };

        mCommentTimer.schedule(mCommentTimerTask, 450, 1000);
    }

    /**
     * Request serial number from CC2564 after 1.1 second delay.
     */
    public void requestSerialNumber() {
        mSerialHandler = new Handler();
        mSerialTimer = new Timer();
        mSerialTimerTask = new TimerTask() {
            @Override
            public void run() {
                mSerialHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        service.write("AL0".getBytes());
                    }
                });
            }
        };
        mSerialTimer.schedule(mSerialTimerTask, 350, 1000);
    }

    /**
     * Request external supply details from CC2564 after 600ms delay
     * once a second.
     */
    public void requestExternalSupply() {
        mExtHandler = new Handler();
        mExternalTimer = new Timer();
        mExternalTimerTask = new TimerTask() {
            @Override
            public void run() {
                mExtHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Request external supply voltage
                        service.write("AY0".getBytes());
                    }
                });
            }
        };
        mExternalTimer.schedule(mExternalTimerTask, 600, 1000);
    }

    /**
     * Request battery voltage and temperature from CC2564 after 500ms delay
     * once a second.
     */
    /*public void requestBattDetails(){
        mBattHandler = new Handler();
        final int delay = 1000;
        mBattHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                service.write("AW0".getBytes());
                mBattHandler.postDelayed(this,delay);
            }
        }, delay);
    }*/
    public void requestBattDetails() {
        mBattHandler = new Handler();
        mBattTimer = new Timer();
        mBattTimerTask = new TimerTask() {
            @Override
            public void run() {
                mBattHandler.post(new Runnable() {
                    public void run() {
                        // Here we wish to update each widget's values once every second
                        // Request batt voltage and batt temperature
                        service.write("AW0".getBytes());
                    }
                });
            }
        };

        mBattTimer.schedule(mBattTimerTask, 450, 1000);
    }
    public void getOnceOffBattV(){
        //Log.d(Constants.DEBUG_TAG, "")
        service.write("AW0".getBytes());
    }

    /**
     * Requests channel data from AML once a second based on msg generated
     * in dashboard activity.
     * @param msg - byte array length 7 containing the command and channel index
     */
    public void requestChannelData(final byte[] msg) {
        // handler for timer task
        mChannelHandler = new Handler();
        // create background timer
        mChannelTimer = new Timer();
        // create Timer Task to execute the request for data to CC2564
        mChTimerTask = new TimerTask() {
            @Override
            public void run() {
                mChannelHandler.post(new Runnable() {
                    public void run() {
                        service.write(msg);
                    }
                });
            }
        };
        // schedule timer for 500ms delay, frequency of 1000ms
        mChannelTimer.schedule(mChTimerTask, 500, 1000);
    }

    /**
     * Cancels any active timers and timerTasks, to eliminate
     * unneeded processes.
     */
    public void cancelActiveTimers() {
        if (mBattTimerTask != null) {
            mBattTimer.cancel();
            mBattTimer.purge();
            mBattTimerTask.cancel();
        }
        if(mCommentTimerTask !=null){
            mCommentTimer.cancel();
            mCommentTimer.purge();
            mCommentTimerTask.cancel();
        }
        if(mExternalTimerTask !=null){
            mExternalTimer.cancel();
            mExternalTimer.purge();
            mExternalTimerTask.cancel();
        }
        if(mSerialTimerTask !=null){
            mSerialTimer.cancel();
            mSerialTimer.purge();
            mSerialTimerTask.cancel();
        }
        if(mNameTimerTask !=null){
            mNameTimer.cancel();
            mNameTimer.purge();
            mNameTimerTask.cancel();
        }
        if(mChTimerTask!=null){
            mChannelTimer.cancel();
            mChannelTimer.purge();
            mChTimerTask.cancel();
        }
    }
}