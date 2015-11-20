package com.carrustruckerapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.retrofit.RestClient;
import com.carrustruckerapp.utils.Log;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyService extends Service implements AppConstants, LocationListener {

    private static String TAG = MyService.class.getSimpleName();
    private MyThread mythread;
    public boolean isRunning = false;
    private String orderId;
    private SharedPreferences sharedPreferences;
    LocationManager locationManager;
    Location location;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    @Override
    public IBinder onBind(Intent arg0) {
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
        orderId = intent.getStringExtra("bookingId");

        Log.d(TAG, "onStart");
        if (!isRunning) {
            mythread.start();
            isRunning = true;
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
                if (!isGPSEnabled && !isNetworkEnabled) {

                } else {
                    if (isNetworkEnabled) {
                        android.util.Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                            }
                        }
                    }
                    if (isGPSEnabled) {
                        if (location == null) {
                            android.util.Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            }
                        }
                    }
                }

                RestClient.getWebServices().sendTracking(orderId,
                        sharedPreferences.getString(DRIVER_NO, ""),
                        String.valueOf(location.getLongitude()),
                        String.valueOf(location.getLatitude()), new Callback<String>() {
                            @Override
                            public void success(String s, Response response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(s);

                                    Intent i = new Intent("custom-event-name");
                                    i.putExtra("bookingStatus", jsonObject.getJSONObject("data").getString("bookingStatus"));
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                                    Log.d("Tracking Success", s);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d("Tracking Failed", "" + error);
                            }
                        });
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