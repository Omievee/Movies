package com.mobile.helpers;

import android.util.Log;

import com.moviepass.BuildConfig;

public class LogUtils {

    public static void newLog(String tag, String message){
        if(BuildConfig.DEBUG){
            Log.d(tag, message);
        }
    }
}
