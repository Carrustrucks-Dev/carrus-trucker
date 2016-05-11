package com.carrus.trucker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.carrus.trucker.R;
import com.carrus.trucker.interfaces.AppConstants;
import com.carrus.trucker.interfaces.GPSDailogCallBack;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Log;
import com.carrus.trucker.utils.Prefs;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class BaseActivity extends FragmentActivity implements GPSDailogCallBack, AppConstants {

    private Activity activity;
    private Dialog dialog;
    protected String accessToken;
    private BroadcastReceiver mMessageReceiver;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        accessToken= Prefs.with(this).getString(ACCESS_TOKEN, null);
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (dialog != null)
                    dialog.dismiss();

                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    showGSP();
                    Log.e("GPS Online Status", "" + false);
                }else{
                    Log.e("GPS Online Status", "" + true);
                }

//
//                boolean enabled = CommonUtils.isGPSEnabled(activity);
//                Log.e("GPS Online Status", "" + enabled);
            }
        };
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("gpstracker"));
        if (!(activity instanceof SplashScreenActivity)) {
            Intent i = new Intent("gpstracker");
            LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if (dialog != null)
            dialog.dismiss();
    }

    /**
     * Method to Hide keyboard On Outside Touch
     * */
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
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
    * Method to show GPS setting dialog
    * */
    @Override
    public void showGSP() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(getString(R.string.gps_dialog_title));
        alertDialog.setMessage(getString(R.string.gps_msg));
        alertDialog.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setCancelable(false);
        dialog = alertDialog.create();
        dialog.show();
    }
}