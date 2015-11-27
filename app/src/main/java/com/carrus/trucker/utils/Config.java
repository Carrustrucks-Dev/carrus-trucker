package com.carrus.trucker.utils;



public class Config {


    static String GCM_PROJECT_NUMBER = "";
    static String BASE_URL = "";
    static AppMode appMode = AppMode.LIVE;
    static String GOOGLE_URL = "";

    static public String getBaseURL() {
        init(appMode);
        return BASE_URL;
    }

    static public String getGoogleUrl(){
        init(appMode);
        return GOOGLE_URL;
    }


    static public String getGCMProjectNumber() {

        init(appMode);

        return GCM_PROJECT_NUMBER;
    }


    /**
     * Initialize all the variable in this method
     *
     * @param appMode
     */
    public static void init(AppMode appMode) {

        switch (appMode) {
            case DEV:
                BASE_URL = "http://52.25.204.93:8080/";
                GCM_PROJECT_NUMBER = "1082788264801";
                GOOGLE_URL = "http://maps.googleapis.com";
                break;

            case TEST:
                BASE_URL = "http://52.25.204.93:3005/";
                GCM_PROJECT_NUMBER = "1082788264801";
                GOOGLE_URL = "http://maps.googleapis.com";
                break;

            case LIVE:
                BASE_URL = "http://52.25.204.93:8080/";
                GCM_PROJECT_NUMBER = "1082788264801";
                GOOGLE_URL = "http://maps.googleapis.com";
                break;
        }

    }

    public enum AppMode {
        DEV, TEST, LIVE
    }

}