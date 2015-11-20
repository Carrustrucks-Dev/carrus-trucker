package com.carrustruckerapp.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectionStatus {

    static Context context;
    private static InternetConnectionStatus instance = new InternetConnectionStatus();
    ConnectivityManager connectManager;

    boolean connected = false;

    public static InternetConnectionStatus getInstance(Context ctx) {
        context = ctx;
        return instance;
    }

    public boolean isOnline() {
        try {
            connectManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable()
                    && networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            System.out
                    .println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }


}
