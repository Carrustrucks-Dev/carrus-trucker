package com.carrus.trucker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.carrus.trucker.utils.Log;


public class LocationStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("GPS","onReceive");
        Intent i = new Intent("gpstracker");

        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
//

//
    }
}
