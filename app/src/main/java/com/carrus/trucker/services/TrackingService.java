package com.carrus.trucker.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.Log;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Developer: SaurabhVerma
 * Dated: 2/11/16.
 */
public class TrackingService extends Service implements AppConstants, LocationListener {

    private static String TAG = MyService.class.getSimpleName();
    private MyThread mythread;
    public boolean isRunning = false;
    private String orderId;
    private SharedPreferences sharedPreferences;
    LocationManager locationManager;
    Location location;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private Context context;

    @Override
    public IBinder onBind(Intent arg0) {
        context = this;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d(TAG, "onCreate");
        context = this;
        mythread = new MyThread();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isRunning) {
            mythread.interrupt();
//            mythread.stop();
        }
    }

    @Override
    public synchronized void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        try {

           /* if (intent.hasExtra("bookingId")) {
                orderId = intent.getStringExtra("bookingId");
                Log.d(TAG, "onStart");
                if (!isRunning && orderId != null && !orderId.trim().isEmpty()) {
                    mythread.start();
                    isRunning = true;
                }
            }*/
            if (!isRunning){
                mythread.start();
                isRunning = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void readWebPage() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class MyThread extends Thread {
        static final long DELAY = 60000;

        @Override
        public void run() {
            while (isRunning) {
                Log.d(TAG, "Running");
                Log.d("OrderId", "" + orderId);

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        mythread.interrupt();
                        return;
                    }
                }

                if (!isGPSEnabled && !isNetworkEnabled) {

                } else {
                    if (isNetworkEnabled) {
                        //android.util.Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                    }
                    if (isGPSEnabled) {
                        //android.util.Log.d("GPS Enabled", "GPS Enabled");
                        if (location==null && locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }

                if (location != null) {
                    RestClient.getWebServices().sendTracking(
                            sharedPreferences.getString(DRIVER_NO, ""),
                            String.valueOf(location.getLongitude()),
                            String.valueOf(location.getLatitude()), new Callback<String>() {
                                @Override
                                public void success(String s, Response response) {
                                        Log.d("Tracking Success", s);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Log.d("Tracking Failed", "" + error);
                                }
                            });
                }
                try {
                    readWebPage();
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    isRunning = false;
                    e.printStackTrace();
                }
            }
        }

    }
}