package com.carrustruckerapp.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.carrustruckerapp.R;
import com.carrustruckerapp.activities.SplashScreen;
import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.utils.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;



public class GcmMessageHandler extends IntentService implements AppConstants {
    private NotificationManager mNotificationManager;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }
    public static void ClearNotification(Context c) {
        Log.v("ClearNotification", "ClearNotification");
        NotificationManager notifManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        Log.v("extras value","--->"+extras.toString());
        try {
            String msg = extras.getString("message");
            sendNotification(msg);
        } catch (Exception e) {
            sendNotification("");
        }
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.toString());
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        final Intent notificationIntent = new Intent(this, SplashScreen.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg)).setAutoCancel(true)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        int id = (int) System.currentTimeMillis();
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        mBuilder.setVibrate(new long[]{1000, 1000});
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}