package com.carrus.trucker.utils;

import android.app.Application;

import com.carrus.trucker.R;
import com.carrus.trucker.interfaces.AppConstants;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Saurbhv on 10/21/15.
 */
public class GlobalClass extends Application implements AppConstants {

//    final String domain = "http://52.25.204.93:8080/";
//    public static final String GOOGLE_URL ="http://maps.googleapis.com";
//    WebServices webServices;
//    Socket mSocket;
//
//    public WebServices getWebServices() {
//        webServices = new RestAdapter.Builder()
//                .setEndpoint(domain).setConverter(new StringConverter()).setLogLevel(RestAdapter.LogLevel.FULL).build().create(WebServices.class);
//        return webServices;
//    }
//
//
//    public WebServices getGoogleWebServices() {
//        webServices = new RestAdapter.Builder()
//                .setEndpoint(GOOGLE_URL).setConverter(new StringConverter()).setLogLevel(RestAdapter.LogLevel.NONE).build().create(WebServices.class);
//        return webServices;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(YOUR_DEFAULT_FONT_PATH)
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}
