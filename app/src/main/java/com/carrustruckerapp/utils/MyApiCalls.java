package com.carrustruckerapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.carrustruckerapp.gcm.GCMClientManager;
import com.carrustruckerapp.interfaces.AppConstants;

/**
 * Created by Saurbhv on 10/27/15.
 */
public class MyApiCalls implements AppConstants {
    GCMClientManager pushClientManager;
    Activity activity;
    Connectivity connectivity;
    SharedPreferences sharedPreferences;
    CommonUtils commonUtils;
    GlobalClass globalClass;

    public MyApiCalls(Activity activity){
        this.activity = activity;
        connectivity = new Connectivity(activity);
        globalClass = (GlobalClass) activity.getApplication();
        sharedPreferences = activity.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        commonUtils=new CommonUtils();
        pushClientManager = new GCMClientManager(activity, SENDER_ID);
    }

    public void getRegistrationId() {
        if (connectivity.isConnectingToInternet()) {
            pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
                @Override
                public void onSuccess(String registrationId, boolean isNewRegistration) {
                    sharedPreferences = activity.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(REGISTRATION_ID, registrationId);
                    editor.commit();
                    Log.v("hello reg_id = ", registrationId);
                }
                @Override
                public void onFailure(String ex) {
                    super.onFailure(ex);
                }
            });
        }
    }
}
