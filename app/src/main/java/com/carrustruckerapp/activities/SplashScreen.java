package com.carrustruckerapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.carrustruckerapp.R;
import com.carrustruckerapp.gcm.GCMClientManager;
import com.carrustruckerapp.interfaces.AppConstants;
import com.carrustruckerapp.interfaces.WebServices;
import com.carrustruckerapp.utils.CommonUtils;
import com.carrustruckerapp.utils.Connectivity;
import com.carrustruckerapp.utils.GlobalClass;
import com.carrustruckerapp.utils.MaterialDesignAnimations;
import com.carrustruckerapp.utils.MyApiCalls;

import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SplashScreen extends BaseActivity implements AppConstants {

    private ProgressBar progressBar;
    public WebServices webServices;
    public GlobalClass globalClass;
    public SharedPreferences sharedPreferences;
    public String accessToken;
    public GCMClientManager pushClientManager;
    public LinearLayout errorLayout;
    public Connectivity connectivity;
    public MyApiCalls myApiCalls;
    public CommonUtils commonUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();

        View.OnClickListener handler = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.retry_button:
                        performAction();
                        break;
                }
            }
        };
        findViewById(R.id.retry_button).setOnClickListener(handler);
        if (sharedPreferences.getBoolean("isFirst", true))
            createShortCut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        performAction();
    }

    private void init() {
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString(ACCESS_TOKEN, "");
        globalClass = (GlobalClass) getApplicationContext();
        webServices = globalClass.getWebServices();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        myApiCalls = new MyApiCalls(SplashScreen.this);
        myApiCalls.getRegistrationId();
        errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
        connectivity = new Connectivity(this);
        commonUtils = new CommonUtils();

//        createDialog();
    }

    private void performAction() {
        if (connectivity.isConnectingToInternet()) {
            afterConfigTrue();
            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.retry_button).setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
        }
    }

    public void afterConfigTrue() {
        webServices.verifyUser(accessToken,
                new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        try {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            JSONObject jsonObject = new JSONObject(serverResponse);
                            JSONObject data = new JSONObject(jsonObject.getString("data"));
                            editor.putString(ACCESS_TOKEN, data.getString("accessToken"));
                            editor.putString(DRIVER_ID, data.getJSONObject("profileData").getString("driverId"));
                            editor.putString(DRIVER_NO, data.getJSONObject("profileData").getString("_id"));
                            editor.putString(DRIVAR_NAME, data.getJSONObject("profileData").getString("driverName"));
                            editor.putString(DRIVING_LICENSE, data.getJSONObject("profileData").getJSONObject("drivingLicense").getString("drivingLicenseNo"));
                            editor.putString(VALIDITY, data.getJSONObject("profileData").getJSONObject("drivingLicense").getString("validity"));
                            editor.putString(DRIVER_PHONENO, data.getJSONObject("profileData").getString("phoneNumber"));
                            editor.putString(DL_STATE, data.getJSONObject("profileData").getString("stateDl"));
                            editor.putString(RATING, data.getJSONObject("profileData").getString("rating"));
                            editor.putString(FLEET_OWNER_NO, data.getJSONObject("profileData").getJSONArray("fleetOwner").getJSONObject(0).getString("phoneNumber"));
//                            editor.putString(EMAIL, data.getString("email"));
                            if (!data.getJSONObject("profileData").isNull("profilePicture")) {
                                JSONObject profilePicture = data.getJSONObject("profileData").getJSONObject("profilePicture");
                                editor.putString(DRIVER_IMAGE, profilePicture.getString("thumb"));
                            } else {
                                editor.putString(DRIVER_IMAGE, null);
                            }
                            editor.commit();
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle mBundle = new Bundle();
                            if (data.has("bookingData")) {
                                mBundle.putBoolean("isBooking",true);
                                mBundle.putString("bookingId", data.getJSONObject("bookingData").getString("_id"));
                                mBundle.putString("tracking", data.getJSONObject("bookingData").getString("tracking"));
                                mBundle.putString("dropOffLong", data.getJSONObject("bookingData").getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLong"));
                                mBundle.putString("dropOffLat", data.getJSONObject("bookingData").getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLat"));
                                mBundle.putString("pickUpLong", data.getJSONObject("bookingData").getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLong"));
                                mBundle.putString("pickUpLat", data.getJSONObject("bookingData").getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLat"));
                            } else {
                                mBundle.putBoolean("isBooking",false);
                            }
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
                        if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
                            MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, getResources().getString(R.string.internetConnectionError), 0);
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
                        } else {
                            commonUtils.showRetrofitError(SplashScreen.this, retrofitError);
                        }
//                        try {
//                            Log.e("request succesfull", "RetrofitError = " + retrofitError.toString());
//                            if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
//                                MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, getResources().getString(R.string.internetConnectionError), 0);
//                                progressBar.setVisibility(View.GONE);
//                                findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
//                            } else {
//                                try {
//                                    String json = new String(((TypedByteArray) retrofitError.getResponse()
//                                            .getBody()).getBytes());
//                                    JSONObject jsonObject = new JSONObject(json);
//                                    int statusCode = retrofitError.getResponse().getStatus();
//                                    if (statusCode == 401) {
//                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
//                                                Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        startActivity(intent);
//                                        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
//                                    } else {
//                                        MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, retrofitError.toString(), 0);
//                                    }
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
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
        sharedPreferences.edit().putBoolean("isFirst", false).commit();
    }

}
