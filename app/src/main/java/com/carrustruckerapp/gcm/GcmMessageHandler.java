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
    public static final int MESSAGE_NOTIFICATION_ID = 435345;

    private NotificationManager mNotificationManager;
    int numMessages;
    //    public static ArrayList<Integer> chatScreenId=new ArrayList<>();
//    public static ArrayList<Integer> bookingScreenId=new ArrayList<>();
    public static String previousChatMessage;
    public static String previousBookingMessage;

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
        // Retrieve data extras from push notification
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        // Keys in the data are shown as extras
        Log.v("extras value", "--->" + extras.toString());
        try {
            String msg = extras.getString("message");
            JSONObject mJSONObject = new JSONObject(extras.getString("flag"));
            String bookingId = mJSONObject.getString("bookingId");
//
            sendNotification(msg, bookingId);
            // Create notification or otherwise manage incoming push

        } catch (Exception e) {
            sendNotification("", "");
        }
        // Log receiving message
        //    Bundle[{message=Checklist has been sent to you. Please verify., android.support.content.wakelockid=1, flag=1, collapse_key=demo, from=799492082381, bookingID=949646}]
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.toString());
        // Notify receiver the intent is completed
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String bookingId) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        final Intent notificationIntent;


        notificationIntent = new Intent(this, BookingDetails.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("bookingId", bookingId);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        mNotificationManager.notify(id, mBuilder.build());
    }


}