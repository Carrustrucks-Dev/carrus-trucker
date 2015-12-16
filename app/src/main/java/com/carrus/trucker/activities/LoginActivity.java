package com.carrus.trucker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.carrus.trucker.R;
import com.carrus.trucker.models.ProfileData;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.InternetConnectionStatus;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.carrus.trucker.utils.MyApiCalls;
import com.carrus.trucker.utils.Prefs;
import com.carrus.trucker.utils.Transactions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etDriverId, etOtp;
    private Button btnSubmit, btnLogin;
    private String driverId, otp;
    private ViewFlipper flipper;
    private Animation slideLeftOut, slideLeftIn, slideRightIn, slideRightOut;
    private boolean flag = false;
    private TextView tvResendOtp;
    private boolean isResendOtp = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUI(getWindow().getDecorView().getRootView());
        init();

        if(Prefs.with(this).getString(ACCESS_TOKEN,"").equals(""))
            //Google Api call to get GCM Key
            new MyApiCalls(LoginActivity.this).getRegistrationId();

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

    /**
     * Method to initialize all the {@link View}s inside the Layout of this
     * {@link Activity}
     */
    private void init() {
        //Get Resource ID from XML
        tvResendOtp = (TextView) findViewById(R.id.tvResendOtp);
        etDriverId = (EditText) findViewById(R.id.etDriverId);
        etOtp = (EditText) findViewById(R.id.etOTP);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        flipper = (ViewFlipper) findViewById(R.id.flipper);

        //Set animation variables
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_left);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_left);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_right);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.anim_slide_out_right);

        //Set Listener
        btnSubmit.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        tvResendOtp.setOnClickListener(this);
    }

    public void onBackPressed() {
        if (flag) {
            flipper.setInAnimation(slideRightIn);
            flipper.setOutAnimation(slideRightOut);
            flipper.showPrevious();
            flag = false;
            etOtp.setText("");
            isResendOtp = false;
        } else {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        }
    }

    /**
     * Method for hide virtual keyboard
     * */
    public void hideKeyboard(View view) {
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void sendDriverId() {
        CommonUtils.showLoadingDialog(LoginActivity.this, getResources().getString(R.string.sending));
        RestClient.getWebServices().checkDriverId(driverId, DEVICE_TYPE, DEVICE_NAME, Prefs.with(this).getString(REGISTRATION_ID, ""),
                new Callback<String>() {
                    @Override
                    public void success(String serverResponse, Response response) {
                        if (!isResendOtp) {
                            flipper.setInAnimation(slideLeftIn);
                            flipper.setOutAnimation(slideLeftOut);
                            flipper.showNext();
                            flag = true;
                        }
                        CommonUtils.dismissLoadingDialog();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (retrofitError.getResponse().getStatus() == 401) {
                            String json = new String(((TypedByteArray) retrofitError.getResponse()
                                    .getBody()).getBytes());
                            try {
                                JSONObject jsonObject = new JSONObject(json);
                                etDriverId.setText("");
                                MaterialDesignAnimations.fadeIn(LoginActivity.this, findViewById(R.id.errorLayout), jsonObject.get("message").toString(), 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            CommonUtils.dismissLoadingDialog();
                        } else {
                            CommonUtils.showRetrofitError(LoginActivity.this, retrofitError);
                            CommonUtils.dismissLoadingDialog();
                        }
                    }
                });
    }

    public void sendOtp() {
        CommonUtils.showLoadingDialog(LoginActivity.this, getResources().getString(R.string.verifying));
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
                            if (profileData.profilePicture != null) {
                                Prefs.with(LoginActivity.this).save(DRIVER_IMAGE, profileData.profilePicture.thumb);
                            }
                            startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class));
                            Transactions.showNextAnimation(LoginActivity.this);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        CommonUtils.dismissLoadingDialog();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (retrofitError.getResponse().getStatus() == 400)
                            etOtp.setText("");
                        CommonUtils.showRetrofitError(LoginActivity.this, retrofitError);
                        CommonUtils.dismissLoadingDialog();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                hideKeyboard(v);
                driverId = etDriverId.getText().toString();
                if (driverId.isEmpty()) {
                    CommonUtils.showSingleButtonPopup(this, getString(R.string.enter_driver_id_msg));
                } else if (driverId.length() < 5) {
                    CommonUtils.showSingleButtonPopup(this, getString(R.string.invalid_driver_id));
                } else {
                    if (InternetConnectionStatus.getInstance(this).isOnline()) {
                        sendDriverId();
                    }
                }
                break;
            case R.id.btnLogin:
                hideKeyboard(v);
                otp = etOtp.getText().toString();
                if (otp.isEmpty()) {
                    CommonUtils.showSingleButtonPopup(this,getString(R.string.enter_otp_msg));
                } else if (otp.length() < 6) {
                    CommonUtils.showSingleButtonPopup(this, getString(R.string.invalid_otp));
                } else {
                    if (InternetConnectionStatus.getInstance(this).isOnline()) {
                        sendOtp();
                    }
                }
                break;

            case R.id.tvResendOtp:
                isResendOtp = true;
                etOtp.setText("");
                sendDriverId();
                break;
        }
    }
}
