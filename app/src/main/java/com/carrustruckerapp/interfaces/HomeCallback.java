package com.carrustruckerapp.interfaces;

import android.content.SharedPreferences;

import com.carrustruckerapp.utils.CommonUtils;

/**
 * Created by Saurbhv on 10/30/15.
 */
public interface HomeCallback {

//    public GlobalClass getGlobalClass();
    public WebServices getWebServices();
    public CommonUtils getCommonUtils();
    public SharedPreferences getSharedPreference();
}
