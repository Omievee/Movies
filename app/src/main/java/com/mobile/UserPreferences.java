package com.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.gson.GsonBuilder;
import com.mobile.model.Movie;
import com.mobile.model.ScreeningToken;
import com.mobile.responses.MicroServiceRestrictionsResponse;
import com.mobile.responses.UserInfoResponse;

import java.util.Calendar;

import javax.annotation.Nullable;

/**
 * Created by ryan on 4/26/17.
 */

public class UserPreferences {

    private static SharedPreferences sPrefs;

    public static void load(Context context) {
        sPrefs = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE);
    }

    public static void saveDeviceAndroidID(String deviceId) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.DEVICE_ANDROID_ID, deviceId);
        editor.apply();
    }

    public static String getDeviceAndroidID() {
        return sPrefs.getString(Constants.DEVICE_ID, "ID");
    }

    public static boolean getHasUserLoggedInBefore() {
        return sPrefs.getBoolean(Constants.IS_USER_FIRST_LOGIN, false);
    }

    public static void hasUserLoggedInBefore(boolean isUserFirstLogin) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putBoolean(Constants.IS_USER_FIRST_LOGIN, isUserFirstLogin);
        editor.apply();
    }

    public static void verifyAndroidIDFirstRun(boolean isAndroidIDVerified) {
        SharedPreferences.Editor edit = sPrefs.edit();
        edit.putBoolean(Constants.IS_ANDROID_ID_VERIFIED, isAndroidIDVerified);
        edit.apply();
    }

    public static boolean getHasUserVerifiedAndroidIDBefore() {
        return sPrefs.getBoolean(Constants.IS_ANDROID_ID_VERIFIED, false);
    }


    public static void saveAAID(String id) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.AAID, id);
        editor.apply();
    }

    public static void updateEmail(String email) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.USER_EMAIL, email);
        editor.apply();
    }

    public static String getAAID() {
        return sPrefs.getString(Constants.AAID, "IDFA");
    }


    public static void setHeaders(String authToken, int user_id) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.USER_AUTH_TOKEN, authToken);
        editor.putInt(Constants.USER_ID, user_id);
        editor.apply();
    }

    public static void setUserCredentials(int userId, String deviceAndroidID, String authToken,
                                          String firstName, String email, @Nullable String oneDeviceID) {
        SharedPreferences.Editor editor = sPrefs.edit();
//
        int id = 3232323;
        String ss = String.valueOf(userId);
        String aa = String.valueOf(id);
        String xx = ss + aa;
//        LogUtils.newLog(Constants.TAG, "setUserCredentials: " + xx);

        editor.putInt(Constants.USER_ID, userId);
        editor.putString(Constants.DEVICE_ANDROID_ID, deviceAndroidID);
        editor.putString(Constants.USER_AUTH_TOKEN, authToken);
        editor.putString(Constants.USER_FIRST_NAME, firstName);
        if (oneDeviceID != null && !oneDeviceID.isEmpty())
            editor.putString(Constants.ONE_DEVICE_ID, oneDeviceID);
        editor.putString(Constants.USER_EMAIL, email);
        editor.apply();
    }


    public static @Nullable String getOneDeviceId() {
        return sPrefs.getString(Constants.ONE_DEVICE_ID, null);
    }

    public static void setOneDeviceId(String id) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.ONE_DEVICE_ID, id);
        editor.apply();
    }

    public static int getUserId() {
        return sPrefs.getInt(Constants.USER_ID, 0);
    }

    public static String getDeviceUuid() {
        return sPrefs.getString(Constants.DEVICE_ANDROID_ID, "device");
    }

    public static String getAuthToken() {
        return sPrefs.getString(Constants.USER_AUTH_TOKEN, "");
    }

    public static String getUserName() {
        return sPrefs.getString(Constants.USER_FIRST_NAME, "bob");
    }

    public static String getUserEmail() {
        return sPrefs.getString(Constants.USER_EMAIL, "email");
    }

    public static void clearUserId() {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putInt(Constants.USER_ID, 0);
        editor.apply();
    }

    public static void setLocation( double lat, double lng) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putLong(Constants.PREFS_LATITUDE, Double.doubleToRawLongBits(lat));
        editor.putLong(Constants.PREFS_LONGITUDE, Double.doubleToRawLongBits(lng));
        editor.apply();
    }

    public static void saveFirebaseHelpshiftToken(String refreshedToken) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.FIREBASE_TOKEN, refreshedToken).apply();
    }

    public static String getFirebaseHelpshiftToken() {
        return sPrefs.getString(Constants.FIREBASE_TOKEN, "null");
    }

    public static void clearEverything() {
        boolean logIn = getHasUserLoggedInBefore();
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.clear().apply();

        hasUserLoggedInBefore(logIn);
    }

    public static void setLastCheckInAttemptDate() {
        SharedPreferences.Editor editor = sPrefs.edit();
        String dateKey = Constants.LAST_CHECK_IN_ATTEMPT_DATETIME + "_" + getUserId();
        editor.putLong(dateKey, System.currentTimeMillis());
        editor.apply();
    }

    public static @Nullable
    Long getLastCheckInAttemptDate() {
        String dateKey = Constants.LAST_CHECK_IN_ATTEMPT_DATETIME + "_" + getUserId();
        long val = sPrefs.getLong(dateKey, -1);
        if (val == -1) {
            return null;
        }
        return val;
    }

    public static Location getLocation() {
        Location location = new Location("current");
        location.setLatitude(getLatitude());
        location.setLongitude(getLongitude());

        return location;
    }

    public static boolean getIsLocationUserDefined() {
        return sPrefs.getBoolean(Constants.IS_LOCATION_USER_DEFINED, false);
    }

    public static boolean getIsSubscriptionActivationRequired() {
        return sPrefs.getBoolean(Constants.IS_SUBSCRIPTION_ACTIVATION_REQUIRED, false);
    }

    public static double getLongitude() {
        return Double.longBitsToDouble(sPrefs.getLong(Constants.PREFS_LONGITUDE, Double.doubleToLongBits(0)));
    }

    public static double getLatitude() {
        return Double.longBitsToDouble(sPrefs.getLong(Constants.PREFS_LATITUDE, Double.doubleToLongBits(0)));
    }

    public static void setPushPermission(boolean pushPermission) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putBoolean(Constants.PUSH_PERMISSION, pushPermission);
        editor.apply();
    }

    public static boolean getPushPermission() {
        return sPrefs.getBoolean(Constants.PUSH_PERMISSION, true);
    }

    public static void setRestrictions(MicroServiceRestrictionsResponse it) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.SUBSCRIPTION_STATUS, it.getSubscriptionStatus().toString());
        editor.putBoolean(Constants.FB_PRESENT, it.getFacebook());
        editor.putBoolean(Constants.THREE_D_ENABLED, it.getHas3d());
        editor.putBoolean(Constants.ALL_FORMATS_ENABLED, it.getHasAllFormats());
        editor.putBoolean(Constants.PROOF_OF_PUCHASE_REQUIRED, it.getProofOfPurchaseRequired());
        editor.putBoolean(Constants.ACTIVE_CARD, it.getHasActiveCard());
        editor.putBoolean(Constants.IS_SUBSCRIPTION_ACTIVATION_REQUIRED, it.getSubscriptionActivationRequired());
        editor.apply();
    }

    public static void setAlertDisplayedId(String alertID) {

        SharedPreferences.Editor edit = sPrefs.edit();
        edit.putString(Constants.ALERT_ID, alertID);
        edit.apply();

    }

    public static String getAlertDisplayedId() {
        return sPrefs.getString(Constants.ALERT_ID, "id");
    }

    public static String getRestrictionSubscriptionStatus() {
        return sPrefs.getString(Constants.SUBSCRIPTION_STATUS, "status");
    }

    public static boolean getRestrictionFacebookPresent() {
        return sPrefs.getBoolean(Constants.FB_PRESENT, true);
    }

    public static boolean getRestrictionThreeDEnabled() {
        return sPrefs.getBoolean(Constants.THREE_D_ENABLED, true);
    }

    public static boolean getRestrictionAllFormatsEnabled() {
        return sPrefs.getBoolean(Constants.ALL_FORMATS_ENABLED, true);
    }

    public static boolean getRestrictionVerificationRequired() {
        return sPrefs.getBoolean(Constants.VERIFICATION_REQUIRED, true);
    }

    public static boolean getRestrictionHasActiveCard() {
        return sPrefs.getBoolean(Constants.ACTIVE_CARD, true);
    }

    public static boolean getProofOfPurchaseRequired() {
        return sPrefs.getBoolean(Constants.PROOF_OF_PUCHASE_REQUIRED, false);
    }

    public static void setFbToken(String token) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.FB_TOKEN, token);
        editor.apply();
    }

    public static void clearFbToken() {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.FB_TOKEN, "token");
        editor.apply();
    }

    public static String getFbToken() {
        return sPrefs.getString(Constants.FB_TOKEN, "token");
    }

    public static void saveReservation(ScreeningToken reservation) {
        if(reservation!=null) {
            String key = Constants.LAST_CHECK_IN_RESERVATION + "_" + getUserId();
            String gson  = new GsonBuilder().create().toJson(reservation);
            sPrefs.edit().putString(key, gson).apply();
        }
    }

    public static void saveBilling(UserInfoResponse userPreferences) {
        if(userPreferences!=null) {
            String key = Constants.BILLING + "_" + getUserId();
            String gson = new GsonBuilder().create().toJson(userPreferences);
            sPrefs.edit().putString(key, gson).apply();
        }
    }

    public static UserInfoResponse getBilling() {
        String key = Constants.BILLING + "_" + getUserId();
        String billing = sPrefs.getString(key,null);
        if(billing!=null) {
            try {
                return new GsonBuilder().create().fromJson(billing, UserInfoResponse.class);
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public static ScreeningToken getLastReservation() {
        String key = Constants.LAST_CHECK_IN_RESERVATION + "_" + getUserId();
        String reservation = sPrefs.getString(key,null);
        if(reservation!=null) {
            try {
                return new GsonBuilder().create().fromJson(reservation, ScreeningToken.class);
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public static void saveTheatersLoadedDate() {
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        sPrefs.edit().putInt(Constants.LAST_DOWNLOADED_THEATERS, dayOfYear).apply();
    }

    public static void saveHistoryLoadedDate() {
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        sPrefs.edit().putInt(Constants.LAST_DOWNLOADED_HISTORY, dayOfYear).apply();
    }


    public static boolean isHistoryLoadedToday() {
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        return sPrefs.getInt(Constants.LAST_DOWNLOADED_HISTORY, -1) == dayOfYear;
    }

    public static void setTotalMoviesSeen(int totalMoviesSeen) {
        sPrefs
                .edit().putInt(Constants.TOTAL_MOVIES_SEEN + "_" + getUserId(), totalMoviesSeen).apply();
    }

    public static void setTotalMoviesSeenLast30Days(int totalMoviesSeenLast30Days) {
        sPrefs
                .edit().putInt(Constants.TOTAL_MOVIES_SEEN_LAST_DAYS + "_" +getUserId(), totalMoviesSeenLast30Days).apply();
    }

    public static void setLastMovieSeen(Movie movie) {
        sPrefs
                .edit().putString(Constants.LAST_MOVIE_SEEN + "_" + getUserId(), movie.getTitle()).apply();
    }

    public static int getTotalMovieSeen() {
        return sPrefs.getInt(Constants.TOTAL_MOVIES_SEEN + "_" + getUserId(), -1);
    }

    public static int getTotalMovieSeenLastMonth() {
        return sPrefs.getInt(Constants.TOTAL_MOVIES_SEEN_LAST_DAYS + "_" +getUserId(),-1);
    }

    public static String getLastMovieSeen() {
        return sPrefs.getString(Constants.LAST_MOVIE_SEEN + "_" + getUserId(), null);
    }
}
