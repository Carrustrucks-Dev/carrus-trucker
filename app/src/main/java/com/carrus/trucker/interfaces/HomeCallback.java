package com.carrus.trucker.interfaces;

import android.content.SharedPreferences;

import com.carrus.trucker.retrofit.WebServices;
import com.carrus.trucker.utils.CommonUtils;

/**
 * Created by Saurbhv on 10/30/15.
 */
public interface HomeCallback {

//    public GlobalClass getGlobalClass();
    public WebServices getWebServices();
    public CommonUtils getCommonUtils();
    public SharedPreferences getSharedPreference();
}
