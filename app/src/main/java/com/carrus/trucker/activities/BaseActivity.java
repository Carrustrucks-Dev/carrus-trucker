package com.carrus.trucker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.interfaces.GPSDailogCallBack;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Log;
import com.carrus.trucker.utils.Prefs;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class BaseActivity extends FragmentActivity implements GPSDailogCallBack, AppConstants {

    private Activity activity;
    private Dialog dialog;
    public String accessToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        accessToken= Prefs.with(this).getString(ACCESS_TOKEN, "");
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (dialog != null)
                    dialog.dismiss();
                boolean enabled = CommonUtils.isGPSEnabled(activity);
                Log.e("GPS Online Status", "" + enabled);
            }
        };
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // to apply uniform customised typeface
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("gpstracker"));
        if (!(activity instanceof SplashScreen)) {
            Intent i = new Intent("gpstracker");
            LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
        }
    }

    private BroadcastReceiver mMessageReceiver;

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if (dialog != null)
            dialog.dismiss();
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.

        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        CommonUtils.hideSoftKeyboard(BaseActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void showGSP() {
        Log.e("showGSP", "showGSP");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setCancelable(false);
//         on pressing cancel button
//       alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });

        // Showing Alert Message
        dialog = alertDialog.create();
        dialog.show();
    }
}