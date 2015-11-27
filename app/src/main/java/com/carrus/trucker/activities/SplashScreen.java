package com.carrus.trucker.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

import com.carrus.trucker.R;
import com.carrus.trucker.entities.ProfileData;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.ApiResponseFlags;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.InternetConnectionStatus;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.carrus.trucker.utils.MyApiCalls;
import com.carrus.trucker.utils.Prefs;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


public class SplashScreen extends BaseActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private AlertDialog.Builder alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();
//        if (Prefs.with(this).getBoolean(IS_FIRST, true))
//            createShortCut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        performAction();
    }

    private void init() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        new MyApiCalls(SplashScreen.this).getRegistrationId();
        findViewById(R.id.retry_button).setOnClickListener(this);
        createDialog();
    }

    private void performAction() {
        if (InternetConnectionStatus.getInstance(this).isOnline()) {
            getAppVersion();
            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.retry_button).setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
        }
    }


    private void getAppVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            CommonUtils.APP_VERSION = info.versionCode + "";
            RestClient.getWebServices().getVersion(USER_TYPE,
                    new Callback<String>() {
                        @Override
                        public void success(String serverResponse, Response response) {

                            try {
                                JSONObject mJSObject = new JSONObject(serverResponse);
                                JSONObject mAndroidVersion = mJSObject.getJSONObject("data").getJSONObject("ANDROID");
                                if (mAndroidVersion.has("criticalVersion")) {
                                    if (Integer.parseInt(mAndroidVersion.getString("criticalVersion")) > info.versionCode) {
                                        alertDialog.setMessage(getString(R.string.critical_update_message));
                                        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("market://details?id=com.carrus.trucker"));
                                                try {
                                                    startActivity(intent);
                                                } catch (Exception e) {
                                                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.carrus.trucker"));
                                                }
                                                dialog.dismiss();
                                            }
                                        });
                                        alertDialog.show();
                                    } else {
                                        if (InternetConnectionStatus.getInstance(SplashScreen.this).isOnline()) {
                                            afterConfigTrue();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                                        }
                                    }

                                } else if (Integer.parseInt(mAndroidVersion.getString("version")) > info.versionCode) {
                                    alertDialog.setMessage(getString(R.string.update_message));
                                    alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id=com.carrus.trucker"));
                                            try {
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.carrus.trucker"));
                                            }
                                            dialog.dismiss();
                                        }
                                    });
                                    alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            if (InternetConnectionStatus.getInstance(SplashScreen.this).isOnline()) {
                                                afterConfigTrue();
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                    alertDialog.show();
                                } else {
                                    if (InternetConnectionStatus.getInstance(SplashScreen.this).isOnline()) {
                                        afterConfigTrue();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            try {
                                if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                                    progressBar.setVisibility(View.GONE);
                                    findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                                } else {
                                    String json = new String(((TypedByteArray) retrofitError.getResponse()
                                            .getBody()).getBytes());
                                    JSONObject jsonObject = new JSONObject(json);
                                    int statusCode = retrofitError.getResponse().getStatus();
                                    if (statusCode == 401) {
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                                    } else {
                                        MaterialDesignAnimations.fadeIn(getApplicationContext(), findViewById(R.id.errorLayout), jsonObject.get("message").toString(), 0);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void afterConfigTrue() {

        RestClient.getWebServices().verifyUser(accessToken,
                new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        try {
                            JSONObject data = new JSONObject(new JSONObject(serverResponse).getString("data"));
                            Gson gson = new Gson();
                            ProfileData profileData = gson.fromJson(data.getString("profileData"), ProfileData.class);
                            Prefs.with(SplashScreen.this).save(ACCESS_TOKEN, data.getString("accessToken"));
                            Prefs.with(SplashScreen.this).save(DRIVER_ID, profileData.driverId);
                            Prefs.with(SplashScreen.this).save(DRIVER_NO, profileData._id);
                            Prefs.with(SplashScreen.this).save(DRIVAR_NAME, CommonUtils.toCamelCase(profileData.driverName));
                            Prefs.with(SplashScreen.this).save(DRIVING_LICENSE, profileData.drivingLicense.drivingLicenseNo);
                            Prefs.with(SplashScreen.this).save(VALIDITY, profileData.drivingLicense.validity);
                            Prefs.with(SplashScreen.this).save(DRIVER_PHONENO, profileData.phoneNumber);
                            Prefs.with(SplashScreen.this).save(DL_STATE, profileData.stateDl);
                            Prefs.with(SplashScreen.this).save(RATING, profileData.rating);
                            Prefs.with(SplashScreen.this).save(FLEET_OWNER_NO, profileData.fleetOwner.get(0).phoneNumber);
                            if (profileData.profilePicture != null) {
                                Prefs.with(SplashScreen.this).save(DRIVER_IMAGE, profileData.profilePicture.thumb);
                            }
//                            if (!data.getJSONObject("profileData").isNull("profilePicture")) {
//                                JSONObject profilePicture = data.getJSONObject("profileData").getJSONObject("profilePicture");
//                                Prefs.with(SplashScreen.this).save(DRIVER_IMAGE, profilePicture.getString("thumb"));
//                            } else {
//                                Prefs.with(SplashScreen.this).save(DRIVER_IMAGE, "null");
//                            }
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle mBundle = new Bundle();
//                            if (data.has("bookingData")) {
//                                mBundle.putBoolean("isBooking", true);
//                                mBundle.putString("bookingId", data.getJSONObject("bookingData").getString("_id"));
//                                mBundle.putString("tracking", data.getJSONObject("bookingData").getString("tracking"));
//                                mBundle.putString("dropOffLong", data.getJSONObject("bookingData").getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLong"));
//                                mBundle.putString("dropOffLat", data.getJSONObject("bookingData").getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLat"));
//                                mBundle.putString("pickUpLong", data.getJSONObject("bookingData").getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLong"));
//                                mBundle.putString("pickUpLat", data.getJSONObject("bookingData").getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLat"));
//                                mBundle.putString("shipperName", data.getJSONObject("bookingData").getJSONObject("shipper").getString("firstName") + " " +
//                                        data.getJSONObject("bookingData").getJSONObject("shipper").getString("lastName"));
//                                mBundle.putString("bookingCreatedAt", data.getJSONObject("bookingData").getJSONObject("pickUp").getString("date"));
//                                mBundle.putString("bookingStatus", data.getJSONObject("bookingData").getString("bookingStatus"));
//                                mBundle.putString("shipperPhoneNumber", data.getJSONObject("bookingData").getJSONObject("shipper").getString("phoneNumber"));
//                                mBundle.putString("shippingJourney", CommonUtils.toCamelCase(data.getJSONObject("bookingData").getJSONObject("pickUp").getString("city")) + " to " +
//                                        CommonUtils.toCamelCase(data.getJSONObject("bookingData").getJSONObject("dropOff").getString("city")));
//                                mBundle.putString("timeSlot", data.getJSONObject("bookingData").getJSONObject("pickUp").getString("time"));
//                                mBundle.putString("truckNameNumber", data.getJSONObject("bookingData").getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName"));
//                            } else {
//                                mBundle.putBoolean("isBooking", false);
//                            }
                            intent.putExtras(mBundle);
                            startActivity(intent);
                            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                        } else {
                            int statusCode = retrofitError.getResponse().getStatus();
                            if (statusCode == ApiResponseFlags.Unauthorized.getOrdinal()) {
                                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                            } else {
                                CommonUtils.showRetrofitError(SplashScreen.this, retrofitError);
                            }
                        }
                    }
                });
    }

    public void createShortCut() {
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), SplashScreen.class));
        sendBroadcast(shortcutintent);
        Prefs.with(this).save(IS_FIRST, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry_button:
                performAction();
                break;
        }
    }

    private void createDialog() {
        alertDialog = new AlertDialog.Builder(SplashScreen.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.update_app_title));
    }
}
