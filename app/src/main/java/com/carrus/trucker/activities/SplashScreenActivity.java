package com.carrus.trucker.activities;

import android.content.Context;
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
import com.carrus.trucker.models.ProfileData;
import com.carrus.trucker.retrofit.RestClient;
import com.carrus.trucker.utils.ApiResponseFlags;
import com.carrus.trucker.utils.CommonUtils;
import com.carrus.trucker.utils.InternetConnectionStatus;
import com.carrus.trucker.utils.Log;
import com.carrus.trucker.utils.MaterialDesignAnimations;
import com.carrus.trucker.utils.MyApiCalls;
import com.carrus.trucker.utils.Prefs;
import com.carrus.trucker.utils.Transactions;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


public class SplashScreenActivity extends BaseActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private AlertDialog.Builder alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();

        //create shortcut on home screen
        if (getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE).getBoolean(IS_FIRST, false))
            createShortCut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        performAction();
    }

    /**
     * Method to initialize all the {@link View}s inside the Layout of this
     * {@link Activity}
     */
    private void init() {
        //Get Resource ID from XML
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //set progress bar color
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

        //Google Api call to get GCM Key
        new MyApiCalls(SplashScreenActivity.this).getRegistrationId();

        //Set Listener
        findViewById(R.id.retry_button).setOnClickListener(this);

        createDialog();
    }

    private void performAction() {
        if (InternetConnectionStatus.getInstance(this).isOnline()) {
            verfiySession();
            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.retry_button).setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
        }
    }

    private void afterConfigTrue(){
        if (InternetConnectionStatus.getInstance(SplashScreenActivity.this).isOnline()) {
            verfiySession();
        } else {
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.retry_button).setVisibility(View.VISIBLE);
        }
    }

    /**
    * API call for app versioning
    * */
    /**
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
                                                goToAppStore();
                                                dialog.dismiss();
                                            }
                                        });
                                        alertDialog.show();
                                    } else {
                                        afterConfigTrue();
                                    }

                                } else if (Integer.parseInt(mAndroidVersion.getString("version")) > info.versionCode) {
                                    alertDialog.setMessage(getString(R.string.update_message));
                                    alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            goToAppStore();
                                            dialog.dismiss();
                                        }
                                    });
                                    alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            afterConfigTrue();
                                        }
                                    });
                                    alertDialog.show();
                                } else {
                                    afterConfigTrue();
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
    }*/

    public void verfiySession() {

        if(accessToken!=null && !accessToken.isEmpty()) {
            RestClient.getWebServices().verifyUser(accessToken,
                    new Callback<String>() {
                        @Override
                        public void success(String serverResponse, Response response) {
                            try {
                                FlurryAgent.onEvent("Session verify mode");
                                final JSONObject data = new JSONObject(new JSONObject(serverResponse).getString("data"));
                                PackageManager manager = SplashScreenActivity.this.getPackageManager();
                                final PackageInfo info = manager.getPackageInfo(SplashScreenActivity.this.getPackageName(), 0);
                                CommonUtils.APP_VERSION = info.versionCode + "";
                                JSONObject mAndroidVersion = data.getJSONObject("appVersion").getJSONObject("ANDROID");

                                if (mAndroidVersion.has("criticalVersion")) {
                                    if (Integer.parseInt(mAndroidVersion.getString("criticalVersion")) > info.versionCode) {
                                        alertDialog.setMessage(getString(R.string.critical_update_message));
                                        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                goToAppStore();
                                                dialog.dismiss();
                                            }
                                        });
                                        alertDialog.show();
                                    } else {
                                        saveDriverInfonRedirect(data);
                                    }

                                } else if (Integer.parseInt(mAndroidVersion.getString("version")) > info.versionCode) {
                                    alertDialog.setMessage(getString(R.string.update_message));
                                    alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            goToAppStore();
                                            dialog.dismiss();
                                        }
                                    });
                                    alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            saveDriverInfonRedirect(data);
                                        }
                                    });
                                    alertDialog.show();
                                } else {
                                    saveDriverInfonRedirect(data);
                                }



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
                                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Transactions.showNextAnimation(SplashScreenActivity.this);
                                } else {
                                    CommonUtils.showRetrofitError(SplashScreenActivity.this, retrofitError);
                                }
                            }
                        }
                    });
        }else{
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            Transactions.showNextAnimation(SplashScreenActivity.this);
        }
    }

    private void saveDriverInfonRedirect(JSONObject data){
        try {
            Gson gson = new Gson();
            ProfileData profileData = gson.fromJson(data.getString("profileData"), ProfileData.class);
            Prefs.with(SplashScreenActivity.this).save(ACCESS_TOKEN, data.getString("accessToken"));
            Prefs.with(SplashScreenActivity.this).save(DRIVER_ID, profileData.driverId);
            Prefs.with(SplashScreenActivity.this).save(DRIVER_NO, profileData._id);
            Prefs.with(SplashScreenActivity.this).save(DRIVAR_NAME, CommonUtils.toCamelCase(profileData.driverName));
            Prefs.with(SplashScreenActivity.this).save(DRIVING_LICENSE, profileData.drivingLicense.drivingLicenseNo);
            Prefs.with(SplashScreenActivity.this).save(VALIDITY, profileData.drivingLicense.validity);
            Prefs.with(SplashScreenActivity.this).save(DRIVER_PHONENO, profileData.phoneNumber);
            Prefs.with(SplashScreenActivity.this).save(DL_STATE, profileData.stateDl);
            Prefs.with(SplashScreenActivity.this).save(RATING, profileData.rating);
            Prefs.with(SplashScreenActivity.this).save(FLEET_OWNER_NO, profileData.fleetOwner.get(0).phoneNumber);
            if (profileData.profilePicture != null) {
                Prefs.with(SplashScreenActivity.this).save(DRIVER_IMAGE, profileData.profilePicture.thumb);
            }
            Intent intent = new Intent(getApplicationContext(), HomeScreenActivity.class);
            Bundle mBundle = new Bundle();
            intent.putExtras(mBundle);
            startActivity(intent);
            Transactions.showNextAnimation(SplashScreenActivity.this);
            finish();
        }catch (Exception e){
            Log.e("Splash Acrtivity", e + "");
        }
    }

    /**
    * Redirect to App Store for Update
    * */
    private void goToAppStore(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.carrus.trucker"));
        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.carrus.trucker"));
        }
    }

    /**
    * Method to create app shortcut on home screen
    * */
    public void createShortCut() {
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), SplashScreenActivity.class));
        sendBroadcast(shortcutintent);
        getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE).edit().putBoolean(IS_FIRST, false).commit();
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
        alertDialog = new AlertDialog.Builder(SplashScreenActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.update_app_title));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, MY_FLURRY_APIKEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
