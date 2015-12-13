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
