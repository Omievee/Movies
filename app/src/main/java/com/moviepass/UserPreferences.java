package com.moviepass;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

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

    public static boolean getIsUserFirstLogin() {
        return sPrefs.getBoolean(Constants.IS_USER_FIRST_LOGIN, false);
    }

    public static void setIsUserFirstLogin(boolean isUserFirstLogin) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putBoolean(Constants.IS_USER_FIRST_LOGIN, isUserFirstLogin);
        editor.apply();
    }

    public static void setUserCredentials(int userId, String deviceUUID, String authToken) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putInt(Constants.USER_ID, userId);
        editor.putString(Constants.USER_DEVICE_UUID, deviceUUID);
        editor.putString(Constants.USER_AUTH_TOKEN, authToken);
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

    public static void setCoordinates(double lat, double lng) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putLong(Constants.PREFS_LATITUDE, Double.doubleToRawLongBits(lat));
        editor.putLong(Constants.PREFS_LONGITUDE, Double.doubleToRawLongBits(lng));
        editor.apply();
    }

    public static void setIsLocationUserDefined(boolean isLocationUserDefined) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putBoolean(Constants.IS_LOCATION_USER_DEFINED, isLocationUserDefined);
        editor.apply();
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

    public static void setRottenTomatoesDisplay(boolean rottenTomatoesDisplay) {
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putBoolean(Constants.ROTTEN_TOMATOES, rottenTomatoesDisplay);
        editor.apply();
    }

    public static boolean getRottenTomatoesDisplay() { return sPrefs.getBoolean(Constants.ROTTEN_TOMATOES, true); }
}
