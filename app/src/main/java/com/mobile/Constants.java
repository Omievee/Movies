package com.mobile;


import com.moviepass.BuildConfig;

import java.util.HashMap;

/**
 * Created by ryan on 4/26/17.
 */

public class Constants {

    public static final String TAG = "Found";
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String TERMS = "https://moviepass.com/content/terms";
    public static final String REDEMPTION_ROUTE = "/coupons/form/redeem";


    //COMMON VARIABLES
    public static final String MOVIE = "movie";
    public static final String TITLE = "title";
    public static final String RESERVATION = "reservation";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String TOKEN = "token";
    public static final String SEAT = "seat";
    public static final String DEVICE_ID = "ID";
    public static final String ONE_DEVICE_ID = "ODID";
    //RESTRICTIONS
    public static final String ACTIVE = "ACTIVE";
    public static final String ACTIVE_FREE_TRIAL = "ACTIVE_FREE_TRIAL";
    public static final String PENDING_FREE_TRIAL = "PENDING_FREE_TRIAL";
    public static final String ENDED_FREE_TRIAL = "ENDED_FREE_TRIAL";
    public static final String CANCELLED = "CANCELLED";
    public static final String CANCELLED_PAST_DUE = "CANCELLED_PAST_DUE";
    public static final String MISSING = "MISSING";
    public static final String PAST_DUE = "PAST_DUE";
    public static final String PENDING_ACTIVATION = "PENDING_ACTIVATION";

    //PERMISSION CODES
    public final static int REQUEST_CAMERA_CODE = 0;
    public static final int REQUEST_STORAGE_CODE = 2;
    public static final int REQUEST_TICKET_VERIF = 00;
    public static final String PREFS_FILE = "com.moviepass.moviepass_preferences";
    public static final String IS_USER_FIRST_LOGIN = "bobloblaw";
    public static final String IS_ANDROID_ID_VERIFIED = "Android";
    /* TODO REMOVE GENERIC VALUES */
    public static final String USER_ID = "0";
    public static final String DEVICE_ANDROID_ID = "androidID";
    public static final String USER_AUTH_TOKEN = "AUTH";
    public static final String FB_TOKEN = "token";
    public static final String AAID = "AAID";
    public static final String USER_FIRST_NAME = "CATHERINE";
    public static final String USER_EMAIL = "BOB@LOBLAW.COM";

    //Location things
    public static final String IS_LOCATION_USER_DEFINED = "usr_loc_defi";
    public static final String PREFS_LATITUDE = "lat";
    public static final String PREFS_LONGITUDE = "lng";
    public static final String LOCATION_UPDATE_INTENT_FILTER = "LOCATION_UPDATE_INTENT_FILTER";
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 00;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 01;

    //Address things
    public static final String CITY_AND_STATE = "city_state";
    public static final HashMap<String, String> STATES_AND_STATES_ABREV = new HashMap<>();
    public static final String ZIP_CODE = "myZip";
    public static final String UNITED_STATES_PREFIX = "US";

    //Restrictions Things
    public static final String SUBSCRIPTION_STATUS = "STATUS";
    public static final String FB_PRESENT = "fb";
    public static final String THREE_D_ENABLED = "threed";
    public static final String ALL_FORMATS_ENABLED = "imax";
    public static final String VERIFICATION_REQUIRED = "verification";
    public static final String ACTIVE_CARD = "card";
    public static final String IS_SUBSCRIPTION_ACTIVATION_REQUIRED = "required";
    public static final String PROOF_OF_PUCHASE_REQUIRED = "proof";

    public static final int CARD_SCAN_REQUEST_CODE = 27;

    public static final String API_RESPONSE_OK = "OK";
    public static final String ROTTEN_TOMATOES = "rt";
    public static final String PUSH_PERMISSION = "push";

    //Taplytics Notifs
    public static final String CUSTOM_DATA = "custom_keys";

    //Check In Attempt
    public static final String LAST_CHECK_IN_ATTEMPT_DATE = "lastCheckInAttemptDate";
    public static final String LAST_CHECK_IN_ATTEMPT_TIME = "lastCheckInAttemptTime";

    //FIREBASE
    public static final String FIREBASE_TOKEN = "firebase_token";

    //ALERT ID
    public static final String ALERT_ID = "id";

    //TICKET VERIFICATION
    public static final String TICKET_VERIFICATION_FAQ_SECTION = "38";


}
