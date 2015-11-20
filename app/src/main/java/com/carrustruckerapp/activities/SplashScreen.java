package com.carrustruckerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ProgressBar;

import com.carrustruckerapp.R;
import com.carrustruckerapp.entities.ProfileData;
import com.carrustruckerapp.retrofit.RestClient;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.InternetConnectionStatus;
import com.carrustruckerapp.utils.MyApiCalls;
import com.carrustruckerapp.utils.Prefs;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SplashScreen extends BaseActivity implements View.OnClickListener {

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();
        if (Prefs.with(this).getBoolean(IS_FIRST, true))
            createShortCut();
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
    }

    private void performAction() {
        if (InternetConnectionStatus.getInstance(this).isOnline()) {
            afterConfigTrue();
            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.retry_button).setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
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
                            if(profileData.profilePicture!=null){
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
                            CommonUtils.showRetrofitError(SplashScreen.this, retrofitError);
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
}
