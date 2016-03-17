package com.aqitapartners.sellers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;

/**
 * Created by Mustafa on 11-01-2016.
 */
public class GCMBroadCastReciever extends BroadcastReceiver {

    private static final String TAG = "GcmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received notification: " + intent.getExtras().toString());
        if (MobiComPushReceiver.isMobiComPushNotification(intent)) {
            Log.i(TAG, "Yes it is a MT notification....");
            MobiComPushReceiver.processMessageAsync(context, intent);
            return;
        }
    }
}
