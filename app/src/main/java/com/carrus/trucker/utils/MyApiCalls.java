package com.carrus.trucker.utils;

import android.app.Activity;

import com.carrus.trucker.gcm.GCMClientManager;
import com.carrus.trucker.interfaces.AppConstants;

/**
 * Created by Saurbhv on 10/27/15.
 */
public class MyApiCalls implements AppConstants {
    private GCMClientManager pushClientManager;
    private Activity activity;

    public MyApiCalls(Activity activity) {
        this.activity = activity;
        pushClientManager = new GCMClientManager(activity, Config.getGCMProjectNumber());
    }

    public void getRegistrationId() {
        if (InternetConnectionStatus.getInstance(activity).isOnline()) {
            pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
                @Override
                public void onSuccess(String registrationId, boolean isNewRegistration) {
                    Prefs.with(activity).save(REGISTRATION_ID, registrationId);
                }

                @Override
                public void onFailure(String ex) {
                    super.onFailure(ex);
                }
            });
        }
    }
}
