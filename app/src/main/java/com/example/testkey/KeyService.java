package com.example.testkey;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class KeyService {
    private static final String LOG_TAG = "KeyService";
    private static int IS_NEED_REDIRECT = 0;
    private static final int BIT_BT_CONNECTED = (1 << 0);
    private static final int BIT_SCREEN_OFF = (1 << 1);
    private static final int NEED_REDIRECT = BIT_BT_CONNECTED | BIT_SCREEN_OFF;
    private final Context mContext;

    public KeyService(Context context) {
        this.mContext = context;
    }

    @SuppressLint("NewApi")
    public static boolean isNeedRedirect(Context context) {
        if (context == null) {
            return false;
        }
        IS_NEED_REDIRECT = 0;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!powerManager.isScreenOn())
            IS_NEED_REDIRECT |= BIT_SCREEN_OFF;
        else
            IS_NEED_REDIRECT &= ~BIT_SCREEN_OFF;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IS_NEED_REDIRECT |= parseBtState(mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET));

        Log.d(LOG_TAG, "IS_NEED_REDIRECT:" + IS_NEED_REDIRECT);
        return IS_NEED_REDIRECT == NEED_REDIRECT;
    }

    @SuppressLint("NewApi")
    public boolean isBtConect() {
        IS_NEED_REDIRECT = 0;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IS_NEED_REDIRECT |= parseBtState(mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET));

        Log.d(LOG_TAG, "IS_NEED_REDIRECT:" + IS_NEED_REDIRECT);
        return (IS_NEED_REDIRECT & BIT_BT_CONNECTED) == BIT_BT_CONNECTED;
    }

    public static int parseBtState(int state) {
        Log.i(LOG_TAG, "state:" + state);
        int result = 0;
        switch (state) {
            case BluetoothAdapter.STATE_CONNECTED:
                Log.i(LOG_TAG, "STATE_CONNECTED");
                result |= BIT_BT_CONNECTED;
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
            case BluetoothAdapter.STATE_DISCONNECTED:
                Log.i(LOG_TAG, "STATE_DISCONNECTING");
                result &= ~BIT_BT_CONNECTED;
                break;
        }
        return result;
    }

    public static String parseKeyCode(int keyCode) {
        String ret = "";
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                ret = "Hello World From Bluetooth";
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                ret = "Hello World From Bluetooth";
                break;
        }
        return ret;
    }

}
