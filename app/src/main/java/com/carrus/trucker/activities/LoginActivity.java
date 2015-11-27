package com.carrus.trucker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.carrus.trucker.R;
import com.carrus.trucker.entities.ProfileData;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.Connectivity;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.carrus.trucker.utils.Prefs;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class LoginActivity extends BaseActivity  {

    private EditText etDriverId, etOtp;
    private Button submitButton, loginButton;
    public String driverId, otp;
    public CommonUtils commonUtils;
    public SharedPreferences sharedPreferences;
    public LinearLayout errorLayout, driverIdLayout, otpLayout;
    ViewFlipper flipper;
    Animation slideLeftOut, slideLeftIn, slideRightIn, slideRightOut;
    boolean flag = false;
    public Connectivity connectivity;
    private TextView resendOtp;
    boolean isResendOtp=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUI(getWindow().getDecorView().getRootView());
        init();
        View.OnClickListener handler = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                switch (v.getId()) {
                    case R.id.submitButton:
                        hideKeyboard(v);
                        driverId = etDriverId.getText().toString();
                        if (driverId.length() == 0) {
                            commonUtils.showSingleButtonPopup(LoginActivity.this, "Please enter driver ID.");
                        } else if (driverId.length() < 6) {
                            commonUtils.showSingleButtonPopup(LoginActivity.this, "Invalid driver ID.");
                        } else {
                            if (connectivity.isConnectingToInternet()) {
                                sendDriverId();
                            }
                        }
                        break;
                    case R.id.loginButton:
                        hideKeyboard(v);
                        otp = etOtp.getText().toString();
                        if (otp.length() == 0) {
                            commonUtils.showSingleButtonPopup(LoginActivity.this, "Please enter OTP.");
                        } else if (otp.length() < 6) {
                            commonUtils.showSingleButtonPopup(LoginActivity.this, "Invalid OTP.");
                        } else {
                            if (connectivity.isConnectingToInternet()) {
                               sendOtp();
                            }
                        }
                        break;

                    case R.id.resendOtp:
                        isResendOtp=true;
                        etOtp.setText("");
                        sendDriverId();
                        break;
                }
            }
        };
        submitButton.setOnClickListener(handler);
        loginButton.setOnClickListener(handler);
        resendOtp.setOnClickListener(handler);


        etDriverId.addTextChangedListener(new TextWatcher() {


            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence txtWatcherStr, int start, int before, int count) {
                if (txtWatcherStr.length() == 6) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etDriverId.getWindowToken(), 0);
                }
            }
        });
        etOtp.addTextChangedListener(new TextWatcher() {


            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence txtWatcherStr, int start, int before, int count) {
                if (txtWatcherStr.length() == 6) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etOtp.getWindowToken(), 0);
                }
            }
        });

    }

    private void init() {
        resendOtp=(TextView) findViewById(R.id.resendOtp);
        connectivity = new Connectivity(this);
        commonUtils = new CommonUtils();
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
        etDriverId = (EditText) findViewById(R.id.driverId);
        etOtp = (EditText) findViewById(R.id.otp);
        submitButton = (Button) findViewById(R.id.submitButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        driverIdLayout = (LinearLayout) findViewById(R.id.driverIdLayout);
        otpLayout = (LinearLayout) findViewById(R.id.otpLayout);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        slideLeftOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_slide_out_left);
        slideLeftIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_slide_in_left);
        slideRightIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_slide_in_right);
        slideRightOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_slide_out_right);
    }

    public void onBackPressed() {
        if (flag) {
            flipper.setInAnimation(slideRightIn);
            flipper.setOutAnimation(slideRightOut);
            flipper.showPrevious();
            flag = false;
            etOtp.setText("");
            isResendOtp=false;
        } else {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        }
    }

//    public class SendDriverId extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            commonUtils.showLoadingDialog(LoginActivity.this, getResources().getString(R.string.sending));
//            sendDriverId();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//        }
//    }
//
//    public class SendDriverIdWithOtp extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            commonUtils.showLoadingDialog(LoginActivity.this, getResources().getString(R.string.verifying));
//            sendOtp();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//        }
//    }

    public void hideKeyboard(View view) {
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void sendDriverId() {
        commonUtils.showLoadingDialog(LoginActivity.this, getResources().getString(R.string.sending));
        RestClient.getWebServices().checkDriverId(driverId, DEVICE_TYPE, DEVICE_NAME, sharedPreferences.getString(REGISTRATION_ID, ""),
                new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        if (!isResendOtp) {
                            try {
                                flipper.setInAnimation(slideLeftIn);
                                flipper.setOutAnimation(slideLeftOut);
                                flipper.showNext();
                                flag = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        commonUtils.dismissLoadingDialog();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (statusCode == 401) {
                            String json = new String(((TypedByteArray) retrofitError.getResponse()
                                    .getBody()).getBytes());
                            try {
                                JSONObject jsonObject = new JSONObject(json);
                                etDriverId.setText("");
                                MaterialDesignAnimations.fadeIn(LoginActivity.this, findViewById(R.id.errorLayout), jsonObject.get("message").toString(), 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            commonUtils.dismissLoadingDialog();
                        } else {
                            commonUtils.showRetrofitError(LoginActivity.this, retrofitError);
                            commonUtils.dismissLoadingDialog();
                        }
//                        try {
//                            Log.e("request succesfull", "RetrofitError = " + retrofitError.toString());
//                            if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
//                                MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, getResources().getString(R.string.internetConnectionError), 0);
//                            } else {
//                                try {
//                                    String json = new String(((TypedByteArray) retrofitError.getResponse()
//                                            .getBody()).getBytes());
//                                    JSONObject jsonObject = new JSONObject(json);
//                                    int statusCode = retrofitError.getResponse().getStatus();
////                                    Log.e("error",retrofitError.toString());
//                                    MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, jsonObject.get("message").toString(), 0);
//                                } catch (Exception e) {
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

    public void sendOtp() {
        commonUtils.showLoadingDialog(LoginActivity.this, getResources().getString(R.string.verifying));
        RestClient.getWebServices().checkOtp(driverId, otp,
                new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        try {
                            JSONObject data = new JSONObject(new JSONObject(serverResponse).getString("data"));
                            Toast.makeText(getApplicationContext(), new JSONObject(serverResponse).getString("message"), Toast.LENGTH_SHORT).show();
                            Gson gson = new Gson();
                            ProfileData profileData = gson.fromJson(data.getString("profileData"), ProfileData.class);
                            Prefs.with(LoginActivity.this).save(ACCESS_TOKEN, data.getString("accessToken"));
                            Prefs.with(LoginActivity.this).save(DRIVER_ID, profileData.driverId);
                            Prefs.with(LoginActivity.this).save(DRIVER_NO, profileData._id);
                            Prefs.with(LoginActivity.this).save(DRIVAR_NAME, profileData.driverName);
                            Prefs.with(LoginActivity.this).save(DRIVING_LICENSE, profileData.drivingLicense.drivingLicenseNo);
                            Prefs.with(LoginActivity.this).save(VALIDITY, profileData.drivingLicense.validity);
                            Prefs.with(LoginActivity.this).save(DRIVER_PHONENO, profileData.phoneNumber);
                            Prefs.with(LoginActivity.this).save(DL_STATE, profileData.stateDl);
                            Prefs.with(LoginActivity.this).save(RATING, profileData.rating);
                            Prefs.with(LoginActivity.this).save(FLEET_OWNER_NO, profileData.fleetOwner.get(0).phoneNumber);
                            if(profileData.profilePicture!=null){
                                Prefs.with(LoginActivity.this).save(DRIVER_IMAGE, profileData.profilePicture.thumb);
                            }
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            JSONObject jsonObject = new JSONObject(serverResponse);
//                            JSONObject data = jsonObject.getJSONObject("data");
//                            editor.putString(ACCESS_TOKEN, data.getString("accessToken"));
//                            editor.putString(DRIVER_ID, data.getJSONObject("profileData").getString("driverId"));
//                            editor.putString(DRIVER_NO, data.getJSONObject("profileData").getString("_id"));
//                            editor.putString(DRIVAR_NAME, data.getJSONObject("profileData").getString("driverName"));
//                            editor.putString(DRIVING_LICENSE, data.getJSONObject("profileData").getJSONObject("drivingLicense").getString("drivingLicenseNo"));
//                            editor.putString(VALIDITY, data.getJSONObject("profileData").getJSONObject("drivingLicense").getString("validity"));
//                            editor.putString(DRIVER_PHONENO, data.getJSONObject("profileData").getString("phoneNumber"));
//                            editor.putString(DL_STATE, data.getJSONObject("profileData").getString("stateDl"));
//                            editor.putString(RATING, data.getJSONObject("profileData").getString("rating"));
////                            editor.putString(EMAIL, data.getString("email"));
//                            if (!data.getJSONObject("profileData").isNull("profilePicture")) {
//                                JSONObject profilePicture = data.getJSONObject("profileData").getJSONObject("profilePicture");
//                                editor.putString(DRIVER_IMAGE, profilePicture.getString("thumb"));
//                            } else {
//                                editor.putString(DRIVER_IMAGE, null);
//                            }
//                            editor.commit();
                            Intent intent = new Intent(LoginActivity.this, HomeScreen.class);
                            Bundle mBundle = new Bundle();
//                            if (data.has("booking")) {
//                                mBundle.putBoolean("isBooking", true);
//                                mBundle.putString("bookingId", data.getJSONObject("booking").getString("_id"));
//                                mBundle.putString("tracking", data.getJSONObject("booking").getString("tracking"));
//                                mBundle.putString("dropOffLong", data.getJSONObject("booking").getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLong"));
//                                mBundle.putString("dropOffLat", data.getJSONObject("booking").getJSONObject("dropOff").getJSONObject("coordinates").getString("dropOffLat"));
//                                mBundle.putString("pickUpLong", data.getJSONObject("booking").getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLong"));
//                                mBundle.putString("pickUpLat", data.getJSONObject("booking").getJSONObject("pickUp").getJSONObject("coordinates").getString("pickUpLat"));
//                                mBundle.putString("shipperName", data.getJSONObject("booking").getJSONObject("shipper").getString("firstName") + " " +
//                                        data.getJSONObject("booking").getJSONObject("shipper").getString("lastName"));
//                                mBundle.putString("bookingCreatedAt", data.getJSONObject("booking").getJSONObject("pickUp").getString("date"));
//                                mBundle.putString("bookingStatus", data.getJSONObject("booking").getString("bookingStatus"));
//                                mBundle.putString("shipperPhoneNumber", data.getJSONObject("booking").getJSONObject("shipper").getString("phoneNumber"));
//                                mBundle.putString("shippingJourney", CommonUtils.toCamelCase(data.getJSONObject("booking").getJSONObject("pickUp").getString("city")) + " to " +
//                                        CommonUtils.toCamelCase(data.getJSONObject("booking").getJSONObject("dropOff").getString("city")));
//                                mBundle.putString("timeSlot", data.getJSONObject("booking").getJSONObject("pickUp").getString("time"));
//                                mBundle.putString("truckNameNumber", data.getJSONObject("booking").getJSONObject("truck").getJSONObject("truckType").getString("typeTruckName"));
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
                        commonUtils.dismissLoadingDialog();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (statusCode == 400) {
                            etOtp.setText("");
                        }
                        commonUtils.showRetrofitError(LoginActivity.this, retrofitError);
//                        try {
//                            Log.e("request succesfull", "RetrofitError = " + retrofitError.toString());
//                            if (((RetrofitError) retrofitError).getKind() == RetrofitError.Kind.NETWORK) {
//                                MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, getResources().getString(R.string.internetConnectionError), 0);
//                            } else {
//                                try {
//                                    String json = new String(((TypedByteArray) retrofitError.getResponse()
//                                            .getBody()).getBytes());
//                                    JSONObject jsonObject = new JSONObject(json);
//                                    int statusCode = retrofitError.getResponse().getStatus();
////                                    Log.e("error",retrofitError.toString());
//                                    MaterialDesignAnimations.fadeIn(getApplicationContext(), errorLayout, retrofitError.toString(), 0);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        commonUtils.dismissLoadingDialog();
                    }
                });
    }
}
