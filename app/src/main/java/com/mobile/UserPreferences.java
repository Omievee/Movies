package com.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.helpshift.util.HelpshiftContext;

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

    public static String getAAID() {
        return sPrefs.getString(Constants.AAID, "IDFA");
    }

    public static void setUserCredentials(int userId, String deviceAndroidID, String authToken,
                                          String firstName, String email) {
        SharedPreferences.Editor editor = sPrefs.edit();

        int us = userId;
        int id = 3232323;
        String ss = String.valueOf(us);
        String aa = String.valueOf(id);
        String xx = ss + aa;
//        Log.d(Constants.TAG, "setUserCredentials: " + xx);

        editor.putInt(Constants.USER_ID, userId);
        editor.putString(Constants.DEVICE_ANDROID_ID, deviceAndroidID);
        editor.putString(Constants.USER_AUTH_TOKEN, authToken);
        editor.putString(Constants.USER_FIRST_NAME, firstName);
        editor.putString(Constants.USER_EMAIL, email);
        editor.apply();
    }

    public static int getUserId() {
        return sPrefs.getInt(Constants.USER_ID, 0);
    }

    public static String getDeviceUuid() {
        return sPrefs.getString(Constants.DEVICE_ANDROID_ID, "device");
    }

    public static String getAuthToken() {
        return sPrefs.getString(Constants.USER_AUTH_TOKEN, "auth");
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

    public static void setLocation(String cityAndState, String zipCode, double lat, double lng, boolean isLocationUserDefined, boolean isSubscriptionActivationRequired) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.CITY_AND_STATE, cityAndState);
        editor.putLong(Constants.PREFS_LATITUDE, Double.doubleToRawLongBits(lat));
        editor.putLong(Constants.PREFS_LONGITUDE, Double.doubleToRawLongBits(lng));
        editor.putString(Constants.ZIP_CODE, zipCode);
        editor.putBoolean(Constants.IS_SUBSCRIPTION_ACTIVATION_REQUIRED, isSubscriptionActivationRequired);
        editor.putBoolean(Constants.IS_LOCATION_USER_DEFINED, isLocationUserDefined);
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
        editor.clear().commit();

        hasUserLoggedInBefore(logIn);
    }

    public static void setLastCheckInAttempt(String date, String time) {
        SharedPreferences.Editor editor = sPrefs.edit();
        String dateKey, timeKey;
        dateKey = Constants.LAST_CHECK_IN_ATTEMPT_DATE + "_" + getUserId();
        timeKey = Constants.LAST_CHECK_IN_ATTEMPT_TIME + "_" + getUserId();
        editor.putString(dateKey, date);
        editor.putString(timeKey, time);
        editor.apply();
    }

    public static String getLastCheckInAttemptDate() {
        String dateKey = Constants.LAST_CHECK_IN_ATTEMPT_DATE + "_" + getUserId();
        return sPrefs.getString(dateKey, "0");
    }

    public static String getLastCheckInAttemptTime() {
        String timeKey = Constants.LAST_CHECK_IN_ATTEMPT_TIME + "_" + getUserId();
        return sPrefs.getString(timeKey, "0");
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

    public static void setRestrictions(String status, boolean fb, boolean threeDEnabled, boolean allFormatsEnabled,
                                       boolean proofOfPurchaseRequired, boolean hasActiveCard, boolean subscriptionRequired) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.SUBSCRIPTION_STATUS, status);
        editor.putBoolean(Constants.FB_PRESENT, fb);
        editor.putBoolean(Constants.THREE_D_ENABLED, threeDEnabled);
        editor.putBoolean(Constants.ALL_FORMATS_ENABLED, allFormatsEnabled);
        editor.putBoolean(Constants.PROOF_OF_PUCHASE_REQUIRED, proofOfPurchaseRequired);
        editor.putBoolean(Constants.ACTIVE_CARD, hasActiveCard);
        editor.putBoolean(Constants.IS_SUBSCRIPTION_ACTIVATION_REQUIRED, subscriptionRequired);
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

    public static void helpshift() {
        HelpshiftContext.getCoreApi().login(String.valueOf(UserPreferences.getUserId()), UserPreferences.getUserName(), UserPreferences.getUserEmail());
    }

}
