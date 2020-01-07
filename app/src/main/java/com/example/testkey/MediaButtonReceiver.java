package com.example.testkey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonReceiver extends BroadcastReceiver {
    private static String TAG = "MediaButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        // Get the KeyEvent object
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        Log.e("==============", event.getKeyCode() + "//");
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {

            // Get key value
            int keycode = event.getKeyCode();
            Toast.makeText(context, keycode + "//", Toast.LENGTH_SHORT).show();
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    break;
                default:
                    break;
            }
        }
    }
}