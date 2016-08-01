package com.chinanetcenter.autoanswer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;

public class ListenerService extends IntentService {

    public static boolean isRunning = false;
    public static final String KEY_WTIME = "wtime";
    private PhoneStateListener listener;
    private int callState = TelephonyManager.CALL_STATE_IDLE;

    public ListenerService() {
        super("ListenerService");
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isRunning = true;
        int timeWait = intent.getIntExtra(KEY_WTIME, 10);
        int count = timeWait;
        synchronized (this) {
            while (true) {
                try {
                    if (callState == TelephonyManager.CALL_STATE_RINGING) {
                        if (count > 0) {
                            count--;
                        } else {
                            count = timeWait;
                            answerCall2();
                        }
                    } else if (count < timeWait) {
                        count = timeWait;
                    }
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ListenerService", "onCreate");
        createListener();
        registerListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("ListenerService", "onDestroy");
        unregisterListener();
        isRunning = false;
    }

    private void createListener() {
        if (listener != null) {
            return;
        }
        listener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                callState = state;
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.i("state", "idle");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.i("state", "offhook");
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.i("state: ", "ringing");
                        break;
                }
            }
        };
    }

    private void registerListener() {
        if (listener != null) {
            TelephonyManager teleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            teleMgr.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            Log.i("ListenerService", "register");
        }
    }

    private void unregisterListener() {
        if (listener != null) {
            TelephonyManager teleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            teleMgr.listen(listener, PhoneStateListener.LISTEN_NONE);
            Log.i("ListenerService", "unregister");
        }
    }

    private void answerCall() {
        // effective only when called immediately after ringing state
        try {
            Log.i("ListenerService: ", "Runtime.exec()");
            Runtime.getRuntime().exec("input keyevent " +
                    Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
        } catch (IOException e) {
            // Runtime.exec had an I/O problem, try to fall back
            Log.e("ListenerService: ", e.getMessage());
        }
    }

    private void answerCall2() {
        Log.i("ListenerService: ", "Intent.ACTION_MEDIA_BUTTON");
        String enforcedPerm = "android.permission.CALL_PRIVILEGED";
        //Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
        //        Intent.EXTRA_KEY_EVENT,
        //        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                Intent.EXTRA_KEY_EVENT,
                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        //sendOrderedBroadcast(btnDown, enforcedPerm);
        sendOrderedBroadcast(btnUp, enforcedPerm);
    }
}
