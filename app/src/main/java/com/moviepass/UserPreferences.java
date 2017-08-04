package com.moviepass;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.StringBuilderPrinter;

/**
 * Created by ryan on 4/26/17.
 */

public class UserPreferences {

    private static SharedPreferences sPrefs;

    public static void load(Context context) {
        sPrefs = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE);
    }

    public static void saveDeviceId(String deviceId) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putString(Constants.USER_DEVICE_UUID, deviceId);
        editor.apply();
    }

    public static boolean getHasUserLoggedInBefore() {
        return sPrefs.getBoolean(Constants.IS_USER_FIRST_LOGIN, false);
    }

    public static void hasUserLoggedInBefore(boolean isUserFirstLogin) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putBoolean(Constants.IS_USER_FIRST_LOGIN, isUserFirstLogin);
        editor.apply();
    }

    public static void setUserCredentials(int userId, String deviceUUID, String authToken,
                                          String firstName, String email) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putInt(Constants.USER_ID, userId);
        editor.putString(Constants.USER_DEVICE_UUID, deviceUUID);
        editor.putString(Constants.USER_AUTH_TOKEN, authToken);
        editor.putString(Constants.USER_FIRST_NAME, firstName);
        editor.putString(Constants.USER_EMAIL, email);
        editor.apply();
    }

    public static int getUserId() {
        return sPrefs.getInt(Constants.USER_ID, 0);
    }

    public static String getDeviceUuid() { return sPrefs.getString(Constants.USER_DEVICE_UUID, "device"); }

    public static String getAuthToken() { return sPrefs.getString(Constants.USER_AUTH_TOKEN, "auth"); }

    public static String getUserName() { return sPrefs.getString(Constants.USER_FIRST_NAME, "bob"); }

    public static String getUserEmail() { return sPrefs.getString(Constants.USER_EMAIL, "email"); }

    public static void clearUserId() {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putInt(Constants.USER_ID, 0);
        editor.apply();
    }

    public static void setLocation(String cityAndState, String zipCode, double lat, double lng, boolean isLocationUserDefined) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putString(Constants.CITY_AND_STATE, cityAndState);
        editor.putLong(Constants.PREFS_LATITUDE, Double.doubleToRawLongBits(lat));
        editor.putLong(Constants.PREFS_LONGITUDE, Double.doubleToRawLongBits(lng));
        editor.putString(Constants.ZIP_CODE, zipCode);
        editor.putBoolean(Constants.IS_LOCATION_USER_DEFINED, isLocationUserDefined);
        editor.apply();
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

    public static boolean getPushPermission() { return sPrefs.getBoolean(Constants.PUSH_PERMISSION, true); }

    public static void setRestrictions(String status, boolean fb, boolean threeDEnabled, boolean allFormatsEnabled,
                                       boolean verificationRequired, boolean hasActiveCard) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putString(Constants.SUBSCRIPTION_STATUS, status);
        editor.putBoolean(Constants.FB_PRESENT, fb);
        editor.putBoolean(Constants.THREE_D_ENABLED, threeDEnabled);
        editor.putBoolean(Constants.ALL_FORMATS_ENABLED, allFormatsEnabled);
        editor.putBoolean(Constants.VERIFICATION_REQUIRED, verificationRequired);
        editor.putBoolean(Constants.ACTIVE_CARD, hasActiveCard);
        editor.apply();
    }

    public static String getRestrictionStatus() {
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

    public static boolean getIsVerificationRequired() {
        return sPrefs.getBoolean(Constants.VERIFICATION_REQUIRED, false);
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
}
