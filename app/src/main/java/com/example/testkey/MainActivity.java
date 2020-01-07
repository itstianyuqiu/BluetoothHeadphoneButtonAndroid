package com.example.testkey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
    Context mContext;
    TextView mTextView;
    private boolean isListener = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mTextView = findViewById(R.id.textView);
        startPlayMusicService();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        // Power
        printToast("Press the Button！");
        //Get the AudioManager object
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //Construct a ComponentName that points to the MediaoButtonReceiver class
        ComponentName mComponent = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        //Register a media receiver for broadcast
        int result = mAudioManager
                .requestAudioFocus(new MyOnAudioFocusChangeListener(),
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result) {//At this point, the focus request is successful
            Toast.makeText(mContext, "Get focus successfully", Toast.LENGTH_SHORT).show();
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                //Register media keys API 21+ (Android 5.0)
                //It's been processed in onresume
            } else {
                //Register media keys API 21-
                mAudioManager.registerMediaButtonEventReceiver(mComponent);
            }
        }
    }

    private void stopPlayMusicService() {
        Intent intent = new Intent(this, PlayerMusicService.class);
        stopService(intent);
    }

    private void startPlayMusicService() {
        Intent intent = new Intent(this, PlayerMusicService.class);
        startService(intent);
    }

    class MyOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.e("===========", "p" + focusChange);
            switch (focusChange) {

                case AudioManager.AUDIOFOCUS_GAIN:
                    Toast.makeText(mContext, "Regain focus", Toast.LENGTH_SHORT).show();
                    // Regain focus, can do to restore playback, restore the background volume of the operation
                    startPlayMusicService();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Toast.makeText(mContext, "Permanently lose focus unless actively retrieved", Toast.LENGTH_SHORT).show();
                    // Permanent loss of focus unless actively reacquired, in which case the focus is taken away by other players, to avoid remixing with other players, music can be paused
                    startPlayMusicService();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Toast.makeText(mContext, "Temporarily lose focus", Toast.LENGTH_SHORT).show();
                    // A temporary loss of focus, in which case other applications have applied for a brief moment of focus, which can lower the background volume
                    startPlayMusicService();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    startPlayMusicService();
                    Toast.makeText(mContext, "Temporarily lose focus", Toast.LENGTH_SHORT).show();
                    // A brief loss of focus, in which case another application has applied for a brief focus in the hope that the other voice will lower the volume (or turn off the voice) to highlight the sound (such as a text message)
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusicService();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isListener) {
            printToast(KeyService.parseKeyCode(keyCode));
        }
        return true;
    }

    public void printToast(String str) {
        mTextView.setText(mTextView.getText().toString() + "\n" + str);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private MediaSession mSession;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        createMediaSession();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaSession();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createMediaSession() {
        Log.v(TAG, "createMediaSession() mSession= " + mSession);
        if (mSession == null) {
            mSession = new MediaSession(mContext, MainActivity.class.getSimpleName());
            mSession.setCallback(mSessionCallback);
            mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            PlaybackState state = new PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_PLAY)
                    .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                    .build();
            mSession.setPlaybackState(state);
            mSession.setActive(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void releaseMediaSession() {
        Log.v(TAG, "releaseMediaSession() mSession=" + mSession);
        if (mSession != null) {
            mSession.setCallback(null);
            mSession.setActive(false);
            mSession.release();
            mSession = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final MediaSession.Callback mSessionCallback = new MediaSession.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaIntent) {
            if (mSession == null || mediaIntent == null) {
                return false;
            }
            if (Intent.ACTION_MEDIA_BUTTON.equals(mediaIntent.getAction())) {
                KeyEvent event = (KeyEvent) mediaIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onKeyDown(event.getKeyCode(), event);
                    onKeyUp(event.getKeyCode(), event);
                    return true;
                }
            }
            return false;
        }
    };
}
