package com.mobile.application;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.crashlytics.android.Crashlytics;
import com.helpshift.All;
import com.helpshift.Core;
import com.helpshift.InstallConfig;
import com.helpshift.exceptions.InstallException;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;

import io.fabric.sdk.android.Fabric;

//import com.taplytics.sdk.Taplytics;


public class Application extends MultiDexApplication {

    static {
        System.loadLibrary("native-lib");
    }

    private static Application mApplication;
    public static final String TAG = "TAG";
    private AmazonS3 s3;
    private static CognitoCachingCredentialsProvider sCredProvider;

//    private native static String getCognitoKey();

    private native static String getCognitoKey();

    static String cognitoPoolId = String.valueOf(getCognitoKey());

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
        Fabric.with(this, new Crashlytics());

//        Taplytics.startTaplytics(this, "setUserAttributes");

        UserPreferences.load(this);

        RestClient.setupAuthenticatedWebClient(getApplicationContext());
        RestClient.setupUnauthenticatedWebClient(getApplicationContext());

        InstallConfig installConfig = new InstallConfig.Builder().build();
        Core.init(All.getInstance());
        try {
            Core.install(this,
                    "aa91c35dbd8884b5f017e42174c2a3a5",
                    "moviepass.helpshift.com",
                    "moviepass_platform_20170118151356463-28066a03efe840b",
                    installConfig);

            String userId = String.valueOf(UserPreferences.getUserId());
            String name = UserPreferences.getUserName();
            String email = UserPreferences.getUserEmail();

            Core.login(userId, name, email);
        } catch (InstallException e) {
            Log.e(TAG, "invalid install credentials : ", e);
        }
    }

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    cognitoPoolId,
                    Regions.US_EAST_1);
        }
        return sCredProvider;
    }

    private void setUpAmazon() {
        s3 = new AmazonS3Client(getCredProvider(getApplicationContext()));
    }

    public AmazonS3 getAmazonS3Client() {
        return s3;
    }
}

