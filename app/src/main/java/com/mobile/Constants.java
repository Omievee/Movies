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
    public static final String POLICY = "policy";
    public static final String RESERVATION_ID = "reservation_id";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";
    public static final String SEAT = "seat";
    public static final String DEVICE_ID = "ID";
    public static final String ONE_DEVICE_ID = "ODID";
    public static final String LAST_DOWNLOADED_THEATERS = "last_dl_theaters";
    public static final String LAST_DOWNLOADED_HISTORY = "last_dl_history";

    //PERMISSION CODES
    public final static int REQUEST_CAMERA_CODE = 0;
    public static final int REQUEST_STORAGE_CODE = 2;
    public static final int SURGE_CHECKOUT_CODE = 3;
    public static final int REQUEST_TICKET_VERIF = 00;
    public static final int REQUEST_LOCATION_FROM_THEATERS_CODE = 4;
    public final static int REQUEST_CAMERA_CODE_FOR_TICKET_VERIFICATION = 5;
    public final static int REQUEST_CAMERA_CODE_FOR_TICKET_VERIFICATION_DENIED = 6;
    public final static int REQUEST_GMS_CAMERA_CODE = 7;
    public static final int ENABLE_LOCATION_CODE = 8;
    public static final int GOOGLE_PLAY_SERVICES_CODE = 1;
    public static final String PREFS_FILE = "com.moviepass.moviepass_preferences";
    public static final String IS_USER_FIRST_LOGIN = "bobloblaw";
    public static final String IS_ANDROID_ID_VERIFIED = "Android";
    public static final String CARD_ACTIVATED_SCREEN =  "activated";
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
    public static final String USER_LOCATION = "userLocation";
    public static final String LOCATION_UPDATE_INTENT_FILTER = "LOCATION_UPDATE_INTENT_FILTER";
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 00;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 01;

    //Address things
    public static final String CITY_AND_STATE = "city_state";
    public static final HashMap<String, String> STATES_AND_STATES_ABREV = new HashMap<>();
    public static final String ZIP_CODE = "myZip";
    public static final String UNITED_STATES_PREFIX = "US";

    //Restrictions Things
    public static final String RESTRICTIONS = "restriction";

    public static final int CARD_SCAN_REQUEST_CODE = 27;

    public static final String API_RESPONSE_OK = "OK";
    public static final String ROTTEN_TOMATOES = "rt";
    public static final String PUSH_PERMISSION = "push";

    //Peak Pricing
    public static final String SHOWN_PEAK_PRICING_ALERT = "shownPeakPricing";

    //Taplytics Notifs
    public static final String CUSTOM_DATA = "custom_keys";

    //Billing
    public static final String BILLING = "billing";

    //Check In Attempt
    public static final String LAST_CHECK_IN_ATTEMPT = "lastCheckInAttempt";
    public static final String LAST_CHECK_IN_RESERVATION = "lastCheckInScreening";

    //Movie History Data
    public static final String TOTAL_MOVIES_SEEN = "totalMoviesSeen";
    public static final String TOTAL_MOVIES_SEEN_LAST_DAYS = "totalMoviesSeenLast30Days";
    public static final String LAST_MOVIE_SEEN = "lastMovieSeen";


    //FIREBASE
    public static final String FIREBASE_TOKEN = "firebase_token";

    //ALERT ID
    public static final String ALERT_ID = "id";

    //TICKET VERIFICATION
    public static final String TICKET_VERIFICATION_FAQ_SECTION = "38";

    public static final double CONVENIENCE_FEE = 1.50;
    public static final int CONVENIENCE_FEE_CENTS = (int) (CONVENIENCE_FEE * 100);

    public static final int OFFSET = 3232323;

    public static final String SHOW_HISTORY_RATING = "historyRating";

    public static final String IS_FROM_RATE_SCREEN = "rate_screen";
}
