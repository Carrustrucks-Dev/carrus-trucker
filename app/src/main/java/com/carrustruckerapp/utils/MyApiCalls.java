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
        pushClientManager = new GCMClientManager(activity, Config.getGCMProjectNumber());
    }

    public void getRegistrationId() {
        if (connectivity.isConnectingToInternet()) {
            pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
                @Override
                public void onSuccess(String registrationId, boolean isNewRegistration) {
                    Prefs.with(activity).save(REGISTRATION_ID, registrationId);
                    Log.v("reg_id",registrationId);
                    Log.v("hello reg_id from shared prefrence= ", Prefs.with(activity).getString(REGISTRATION_ID, ""));
                }
                @Override
                public void onFailure(String ex) {
                    super.onFailure(ex);
                }
            });
        }
    }
}
