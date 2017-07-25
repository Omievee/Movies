package com.moviepass.application;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.moviepass.Constants;
import com.moviepass.UserPreferences;
import com.moviepass.network.RestClient;
import com.taplytics.sdk.Taplytics;


public class Application extends MultiDexApplication {

    static {
        System.loadLibrary("native-lib");
    }

    private static Application mApplication;
    public static final String TAG = "TAG";
    private AmazonS3 s3;
    private  static CognitoCachingCredentialsProvider sCredProvider;

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

        Taplytics.startTaplytics(this, "setUserAttributes");

        UserPreferences.load(this);

        RestClient.setupAuthenticatedWebClient(getApplicationContext());
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

