package com.moviepass;

import java.util.HashMap;

/**
 * Created by ryan on 4/26/17.
 */

public class Constants {

    public static final boolean DEBUG = true;
    public static final String ENDPOINT = "http://android.moviepass.com";
    public static final String TERMS = "http://moviepass.com/content/terms";
    public static final String REDEMPTION_ROUTE = ENDPOINT + "/coupons/form/redeem";


    public static final String PREFS_FILE = "com.moviepass.moviepass_preferences";

    public static final String IS_USER_FIRST_LOGIN = "bobloblaw";

    /* TODO REMOVE GENERIC VALUES */
    public static final String USER_ID = "0";
    public static final String USER_DEVICE_UUID = "UUID";
    public static final String USER_AUTH_TOKEN = "AUTH";

    public static final String USER_FIRST_NAME = "CATHERINE";
    public static final String USER_EMAIL = "BOB@LOBLAW.COM";

    //Location things
    public static final String IS_LOCATION_USER_DEFINED = "usr_loc_defi";
    public static final String PREFS_LATITUDE = "lat";
    public static final String PREFS_LONGITUDE = "lng";
    public static final String LOCATION_UPDATE_INTENT_FILTER = "LOCATION_UPDATE_INTENT_FILTER";

    //Address things
    public static final String CITY_AND_STATE = "city_state";
    public static final HashMap<String, String> STATES_AND_STATES_ABREV = new HashMap<>();
    public static final String ZIP_CODE = "zip";
    public static final String UNITED_STATES_PREFIX = "US";


    public static final String API_RESPONSE_OK = "OK";
    public static final String ROTTEN_TOMATOES = "rt";

}
