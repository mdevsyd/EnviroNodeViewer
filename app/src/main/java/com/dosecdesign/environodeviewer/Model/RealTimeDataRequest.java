package com.dosecdesign.environodeviewer.Model;

import android.os.Handler;

import com.dosecdesign.environodeviewer.Services.BtLoggerSPPService;

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
    private TimerTask mExternalTimerTask, mCommentTimerTask, mSerialTimerTask, mNameTimerTask, mBattTimerTask, mChTimerTask;


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
        mNameTimer.schedule(mNameTimerTask, 1000, 1000);
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

        mCommentTimer.schedule(mCommentTimerTask, 1050, 1000);
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
        mSerialTimer.schedule(mSerialTimerTask, 1100, 1000);
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
     * Request battery voltage and temperature form CC2564 after 500ms delay
     * once a second.
     */
    public void requestBattDetails() {
        mBattHandler = new Handler();
        mBattTimer = new Timer();
        mBattTimerTask = new TimerTask() {
            @Override
            public void run() {
                mBattHandler.post(new Runnable() {
                    public void run() {
                        // Here we wish to update each widget's values once every 5 secs
                        // Request batt voltage and batt temperature
                        service.write("AW0".getBytes());

                    }
                });
            }
        };

        mBattTimer.schedule(mBattTimerTask, 500, 1000);
    }

    /**
     * Requests channel data from AML once a second for the selected channel
     * @param msg - byte array containing the command and channel index
     */
    public void requestLiveChannelData(final byte[] msg) {
        mChannelHandler = new Handler();
        mChannelTimer = new Timer();

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
