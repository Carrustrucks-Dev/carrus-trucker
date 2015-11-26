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
import android.util.Log;

import com.carrustruckerapp.R;
import com.carrustruckerapp.activities.BookingDetails;
import com.carrustruckerapp.interfaces.AppConstants;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;


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
        Log.v("extras value", "--->" + extras.toString());
        try {
            JSONObject mJSONObject = new JSONObject(extras.getString("flag"));
            sendNotification(extras.getString("message"), mJSONObject.getString("bookingId"), extras.getString("brand_name"));
        } catch (Exception e) {
            sendNotification("", "", "");
        }
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.toString());
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String bookingId, String brandName) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        final Intent notificationIntent;


        notificationIntent = new Intent(this, BookingDetails.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("bookingId", bookingId);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.notification_icon).setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                        .setContentTitle(brandName)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg)).setAutoCancel(true)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        int id = (int) System.currentTimeMillis();
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        mBuilder.setVibrate(new long[]{1000, 1000});
        mNotificationManager.notify(id, mBuilder.build());
    }


}