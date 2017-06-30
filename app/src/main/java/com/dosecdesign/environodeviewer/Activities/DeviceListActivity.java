package com.dosecdesign.environodeviewer.Activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dosecdesign.environodeviewer.R;
import com.dosecdesign.environodeviewer.Utitilies.Constants;

import java.util.ArrayList;
import java.util.Set;

/**
 *  Activity to show the user a list of paired devices and an option to scan for new devices. New devices are added
 *  to a list and user can select any device to attempt a connection.
 */

public class DeviceListActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, PermissionResultCallback {

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;
    private ArrayAdapter mNewDevicesArrayAdapter;
    private View mLayout;

    // list of permissions

    ArrayList<String> permissions=new ArrayList<>();

    PermissionUtils permissionUtils;

    /**
     * Sets up the view and click listener for scan button. Checks bluetooth compatability, registers
     * broadcast receivers and requests paired devices from host device.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        ///////////////////// TESTING

        permissionUtils=new PermissionUtils(DeviceListActivity.this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);


        /////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Button scanBtn = (Button) findViewById(R.id.button_scan);

        mLayout = findViewById(R.id.mainLayout);

        setResult(Activity.RESULT_CANCELED);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        //permissionUtils.checkPermission(permissions,"Your permission is required to use Bluetooth on this device",1);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionUtils.checkPermission(permissions,"Explain here why the app needs permissions",1);

                // TODO comment this back in!!!!!!!!!
                 //startDiscovery();

                // make the current button disappear
                v.setVisibility(View.GONE);
            }
        });
        checkBtCompatible();
        //enableBt();
        // Setup up the two array adapters one for each type of device list. (paried and discovered).
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);


        //obtain currently paired device list, if any, add them to the ArrayAdapter
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        //register to receive broadcasts when a device is found
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // register to receive broadcast when the discovery is finished
        filter = new IntentFilter((BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        this.registerReceiver(mReceiver, filter);

        createDiscoverabilityFilter();

        getPairedDevices();


    }

    /**
     * Create filter and register to receive notification of discoverabilty changes
     * (from http://stackoverflow.com/questions/30222409/android-broadcast-receiver-bluetooth-
     * events-catching/30292660#30292660)
     */
    private void createDiscoverabilityFilter() {
        IntentFilter discoverabilityFilter = new IntentFilter();
        discoverabilityFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoverabilityFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoverabilityFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver2, discoverabilityFilter);
    }

    private Set<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDev = getResources().getText(R.string.no_paired_devices).toString();
            mPairedDevicesArrayAdapter.add(noDev);
        }
        return pairedDevices;
    }

    /**
     * Creates an intent to enable Bluetooth on the host device if it is not yet enabled
     */
    private void enableBt() {
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    /**
     * Alerts the user if Bluetooth is not available on their device
     */
    private void checkBtCompatible() {
        //check if BT is supported on host device
        if (mBtAdapter == null) {
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Method to redirect to permissions utility, and extract permissions result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }


    /**
     * On click listener for the paired and new devices in lists. Cancels discovery if item is clicked.
     * Sends the name and address of the device selected by the user to the next activity.
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //need to cancel the discovery as it is resource heavy
            mBtAdapter.cancelDiscovery();

            // MAC hardware address of the device is the last 17 chars of the view
            String info = ((TextView) view).getText().toString();

            if (info.length() >= 17) {
                String address = info.substring(info.length() - 17);
                String name = info.substring(0, info.length() - 17);

                //Toast.makeText(DeviceListActivity.this, name + ", " + address, Toast.LENGTH_SHORT).show();

                //enable local device discoverability - only necessary when connecting two android devices
                //enableHostDiscoverability();

                //create intent including the hardware address

                Intent commsIntent = new Intent(DeviceListActivity.this, BluetoothAmlDashboardActivity.class);

                commsIntent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, address);
                commsIntent.putExtra(Constants.EXTRA_DEVICE_NAME, name);
                //set Result, end this activity and start BTDataTransferActivity
                setResult(RESULT_OK, commsIntent);
                startActivity(commsIntent);

            } else {
                setResult(RESULT_CANCELED);
            }


        }
    };

    /**
     * Makes the host device discoverable to other BT devices for 2 mins (120 seconds)
     */
    private void enableHostDiscoverability() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivityForResult(discoverableIntent, Constants.REQUEST_HOST_DISCOVERABILITY);

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the action to check if device was found
            String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:


                case BluetoothDevice.ACTION_FOUND:

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //check if the device has already paired
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (mNewDevicesArrayAdapter.getCount() == 0) {
                        //change display to show no devices were found
                        String noDev = getResources().getText(R.string.no_discovered_device).toString();
                        mNewDevicesArrayAdapter.add(noDev);
                        mBtAdapter.cancelDiscovery();
                    }
                    Button scanBtn = (Button) findViewById(R.id.button_scan);
                    scanBtn.setVisibility(View.VISIBLE);
                    mBtAdapter.cancelDiscovery();
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:

                    if (mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                        enableBt();
                    }
                    break;
            }
        }
    };

    /**
     * Broadcast receiver to listen for changes to the state of the connectability of the Bluetooth Adapter.
     */
    private BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                //int previous = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (scanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(DeviceListActivity.this, R.string.connectable, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(DeviceListActivity.this, R.string.connectable_discoverable, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(DeviceListActivity.this, R.string.discovery_error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    /**
     * Un registers receivers during the on pause state, as they won't be needed
     * whilst users are not utilising the application
     */
    @Override
    protected void onPause() {
        super.onPause();
        //unregister listeners to the broadcasts
        try {
            this.unregisterReceiver(mReceiver);
            this.unregisterReceiver(mReceiver2);
        } catch (IllegalArgumentException e) {
            Log.d(Constants.DEBUG_TAG, "Catch error: " + e);
        }
    }

    /**
     * Setup BT is it is not already upon resuming an activity (if user navigates away)
     */
    @Override
    protected void onResume() {
        super.onResume();
        //enable BT on device
        enableBt();
        //getPairedDevices();
    }

    /**
     * Cancels any active discoveries and de-registers broadcast receivers
     * when activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // always cancel the discovery
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        //unregister listeners to the broadcasts
        try {
            this.unregisterReceiver(mReceiver);
            this.unregisterReceiver(mReceiver2);
        } catch (IllegalArgumentException e) {
            Log.d(Constants.DEBUG_TAG, "Catch error: " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check requestCode to know what requested the result
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth successfully enabled.", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Bluetooth enabling failed. Exiting.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case Constants.REQUEST_HOST_DISCOVERABILITY:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(DeviceListActivity.this, R.string.discovery_error, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    /**
     * OVerrides from the permissions interface, only allow discovery once Bluetooth permissions have been given.
     * @param request_code
     */
    @Override
    public void PermissionGranted(int request_code) {
        Log.d("PERMISSION","GRANTED");
        if(mBtAdapter.isDiscovering()){
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();

    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY","GRANTED");

    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION","DENIED");

    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION","NEVER ASK AGAIN");

    }
}
