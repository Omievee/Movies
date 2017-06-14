package com.moviepass.application;

import android.support.multidex.MultiDexApplication;

import com.moviepass.UserPreferences;
import com.moviepass.network.RestClient;


public class Application extends MultiDexApplication {

    private static Application mApplication;
    public static final String TAG = "TAG";

    public static Application getInstance() {
        return mApplication;
    }

    public Application() {
        super();
        mApplication = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UserPreferences.load(this);

        RestClient.setupAuthenticatedWebClient(getApplicationContext());
    }
}

