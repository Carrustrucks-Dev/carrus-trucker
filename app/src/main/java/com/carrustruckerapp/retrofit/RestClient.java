package com.carrustruckerapp.retrofit;

import com.carrustruckerapp.interfaces.WebServices;
import com.carrustruckerapp.utils.Config;
import com.carrustruckerapp.utils.StringConverter;

import retrofit.RestAdapter;

/**
 * Created by Saurbhv on 11/20/15.
 */
public class RestClient {
    private static WebServices webServices = null;
    private static WebServices googleWebServices = null;

    public static WebServices getWebServices() {
        if (webServices == null) {
            webServices = new RestAdapter.Builder()
                    .setEndpoint(Config.getBaseURL()).setConverter(new StringConverter()).setLogLevel(RestAdapter.LogLevel.FULL).build().create(WebServices.class);

        }
        return webServices;
    }

    public static WebServices getGoogleApiService() {
        if (googleWebServices == null) {
            googleWebServices = new RestAdapter.Builder()
                    .setEndpoint(Config.getGoogleUrl()).setConverter(new StringConverter()).setLogLevel(RestAdapter.LogLevel.NONE).build().create(WebServices.class);
        }
        return googleWebServices;

    }
}
