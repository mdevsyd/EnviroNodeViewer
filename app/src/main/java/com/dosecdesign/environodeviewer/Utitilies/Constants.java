package com.dosecdesign.environodeviewer.Utitilies;

import android.Manifest;

import java.util.UUID;

/**
 * Created by Michi on 5/05/2017.
 */

public class Constants {

    // Debug TAG
    public static final String DEBUG_TAG = "debug";
    public static final String ERROR_TAG = "error";

    // Bluetooth Device Constants
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static final String LOCAL_DEVICE_NAME = "local_device_name";

    // UUID Constants

    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // BT request Constants
    public static final int REQUEST_ENABLE_BT = 100;
    public static final int REQUEST_CONNECT_DEVICE = 200;
    public static final int REQUEST_HOST_DISCOVERABILITY = 400;

    // Messages sent from BT service handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Names received from the msg handler
    public static final String TOAST = "toast";
    public static final String DEVICE_NAME = "device_name";

    // Http constants
    public static final String HTTP_SCHEME = "https";
    public static final String HTTP_AUTHORITY = "api.ictcommunity.org";
    public static final String HTTP_PATH_1 = "v0";
    public static final String HTTP_PATH_2 = "Accounts";
    public static final String HTTP_PATH_3 = "A3JHBLG1ZBYLK63Q";


    // Persistent memory Constants
    public static final int READ_BLOCK_SIZE = 100;

    // Intent constants
    public static final String SELECTED_HUB = "selected_hub";
    public static final String SELECTED_INSTRUMENT = "selected_inst";
    public static final String SELECTED_CHANNEL_NAME = "selected_ch_name";
    public static final String SELECTED_CHANNELS = "selected_channels";
    public static final String SEL_CH_ARRAY = "selected_channels_array";
    public static final String DATESTAMP_ARRAY = "query_datestamp_array";
    public static final String STRING_RESULT = "query_result";

    // Permission Constants
    public static final int REQUEST_CODE_ASK_BT_PERMISSIONS = 999;
    public static String[] PERMISSIONS_BLUETOOTH = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


    // Live Data Constants
    public static final String LIVE_DATA_TYPE = "live_data_type";

    // Service Constants
    public static final String ACTION = "spp_service_action_string";

}