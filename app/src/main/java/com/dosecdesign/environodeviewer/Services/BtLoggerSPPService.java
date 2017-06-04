package com.dosecdesign.environodeviewer.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import static com.dosecdesign.environodeviewer.Utitilies.Constants.*;


/**
 * This class manages all Bluetooth Connections with
 * external devices. It has a thread listening for
 * incoming connections, a thread for connecting to devices
 * and a thread for transmitting data.
 */

public class BtLoggerSPPService {

    // fields
    private int mState;
    //private final int mNewState;
    private BluetoothAdapter mBtAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private ListView mDataView;
    private Context mContext;

    private boolean mAllowInsecureConnections;


    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final String TOAST = "toast";

    // Constructor :                            //
    // Context - the UI context                 //
    // handles - used to send msgs back to UI   //
    public BtLoggerSPPService(Context context, Handler handler) {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        //mDataView = dataView;
        mContext = context;
        mAllowInsecureConnections = true;
    }

    /**
     * Sets the state of the current connection.
     *
     * @param state - int describing current state.
     */
    private synchronized void setState(int state) {
        mState = state;

        //update handler to recieve the new state and update the UI
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the Bluetooth SPP Service.
     * This starts to listen for incoming connection requests (server mode)
     * Source:
     */
    public synchronized void start() {

        //cancel any other threads attempting a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //cancel any running connections
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);

    }

    /**
     * Start the connectThread to initiate a connection to the device
     *
     * @param device - BT device to connect to
     */
    public synchronized void connect(BluetoothDevice device) {

        // Cancel any thread attempting a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device and change state.
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing the bluetooth connection.
     *
     * @param socket - BT socket on which connection was made
     * @param device - BT device which has successfully been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that initiated the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread. Manage the connection and the data throughput
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the handler the name of the device you have connected to
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME, device.getName());
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Update the connection state
        setState(STATE_CONNECTED);
    }


    /**
     * Stop all active threads and update the state
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the connectedThread
     *
     * @param out - byte to send
     */
    public void write(byte[] out) {
        //Log.d(Constants.DEBUG_TAG, "service --> write: "+ out);

        //temp object
        ConnectedThread x;

        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            x = mConnectedThread;
        }
        x.write(out);
    }

    private void connectionFailed() {
        setState(STATE_NONE);

        //Send a failed connection message back to the UI
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect");
        //bundle.putString(TOAST, mContext.getString(R.string.toast_unable_to_connect));
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }


    /**
     * This thread runs while attempting an outgoing connection with
     * a device. Source: https://developer.android.com/guide/topics/connectivity/bluetooth.html
     * TODO finish this
     */

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                if ( mAllowInsecureConnections ) {
                    Method method;

                    method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                }
                else {
                    tmp = device.createRfcommSocketToServiceRecord( Constants.SPP_UUID );
                }
            } catch (Exception e) {
            }
            mmSocket = tmp;
        }

        public void run() {

            setName("ConnectThread");

            // Cancel discovery because it otherwise slows down the connection.
            mBtAdapter.cancelDiscovery();

            //connect to BT socket
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.

                // update UI to alert of failed connection
                connectionFailed();

                // Attempt to close socket if failed
                try {
                    mmSocket.close();

                } catch (IOException closeException) {
                    Log.e(DEBUG_TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);

            // Reset the ConnectThread because we're done
            synchronized (BtLoggerSPPService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }


        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(Constants.DEBUG_TAG, "close() of connect socket failed", e);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(Constants.DEBUG_TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;

            // Listen to the inputstream while connected

            while (true) {
                try {
                    // Read from the input stream
                    bytes = mmInStream.read(buffer);
                    //mDataView.write(buffer, bytes);

                    //TODO this could be incorrect
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(Constants.DEBUG_TAG, "disconnected", e);
                    connectionLost();
                    break;

                }
            }
        }

        public void write(byte[] buffer) {
            try {
                //Log.d(Constants.DEBUG_TAG,"service ConnectedThread write: "+buffer);
                mmOutStream.write(buffer);


                // share sent message back to UI activity
                mHandler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer)
                        .sendToTarget();

            } catch (IOException e) {
                Log.e(DEBUG_TAG, "exception caught while writting", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "failed to close() socket");
            }
        }
    }

    private void connectionLost() {
        setState(STATE_NONE);

        // Send a message to the handler to alert of lost connection
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, mContext.getString(R.string.toast_connection_lost));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void setAllowInsecureConnections(boolean allowInsecureConnections) {
        mAllowInsecureConnections = allowInsecureConnections;
    }

    public boolean getAllowInsecureConnections() {
        return mAllowInsecureConnections;
    }

}
