package com.moviepass;

import java.util.HashMap;

/**
 * Created by ryan on 4/26/17.
 */

public class Constants {

    public static final boolean DEBUG = true;
    //public static final String ENDPOINT = "http://moviepass-ti.herokuapp.com";
    public static final String ENDPOINT = "http://android.moviepass.com";
    //public static final String ENDPOINT = "http://staging.moviepass.com";
    //public static final String ENDPOINT = "http://ti.moviepass.com";
    public static final String TERMS = "http://moviepass.com/content/terms";
    public static final String REDEMPTION_ROUTE = ENDPOINT + "/coupons/form/redeem";


    public static final String PREFS_FILE = "com.moviepass.moviepass_preferences";

    public static final String IS_USER_FIRST_LOGIN = "bobloblaw";

    public static final String USER_ID = "user_id";
    public static final String USER_DEVICE_UUID = "user_device_id";
    public static final String USER_AUTH_TOKEN = "user_auth_token";


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

}
