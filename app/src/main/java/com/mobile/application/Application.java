package com.mobile.application;

import android.app.Activity;
import android.util.Log;

import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.support.AppboyLogger;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.helpshift.All;
import com.helpshift.Core;
import com.helpshift.InstallConfig;
import com.helpshift.exceptions.InstallException;
import com.mobile.UserPreferences;
import com.mobile.analytics.AnalyticsManager;
import com.mobile.di.DaggerAppComponent;
import com.mobile.helpshift.HelpshiftHelper;
import com.mobile.network.RestClient;
import com.mobile.utils.FastStack;
import com.moviepass.BuildConfig;
import com.taplytics.sdk.Taplytics;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;

public class Application extends android.app.Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    @Inject
    Gson gson;

    @Inject
    AnalyticsManager analyticsManager;

    private Activity firstActivity;

    private static Application mApplication;
    public static final String TAG = "TAG";

    public static Application getInstance() {
        return mApplication;
    }

    private FastStack<Activity> activityStack = new FastStack<>();

    public Application() {
        super();
        mApplication = this;
    }

    @Nullable
    public Activity getCurrentActivity() {
        return activityStack.peek();
    }

    public FastStack<Activity> getActivityStack() {
        return activityStack;
    }

    //giguyigfyug
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());

        if (BuildConfig.DEBUG) {
            AppboyLogger.setLogLevel(Log.VERBOSE);
        }
        Taplytics.startTaplytics(this, "3629c653bc0ece073faa45be6fa7081561426e87");

        inject();
        UserPreferences.INSTANCE.load(this, gson);
        Fabric.with(this, new Crashlytics());
        Fresco.initialize(this);
        RestClient.setupAuthenticatedWebClient(getApplicationContext());
        RestClient.setupAuthenticatedGoWatchIt(getApplicationContext());
        RestClient.setUpRegistration(getApplicationContext());
        RestClient.setupMicroService(getApplicationContext());
        InstallConfig installConfig = new InstallConfig.Builder().build();
        Core.init(All.getInstance());


        registerActivityLifecycleCallbacks(new ActivigtyCallbacks() {
            @Override
            public void onActivityResumed(@Nullable Activity activity) {
                activityStack.push(activity);
                if (firstActivity == null) {
                    firstActivity = activity;
                    analyticsManager.onAppOpened();
                }
            }

            @Override
            public void onActivityDestroyed(@Nullable Activity activity) {
                if (firstActivity == activity) {
                    return;
                }
                activityStack.remove(activity);
            }
        });
        try {
            Core.install(this,
                    "d7307fbf50724282a116acadd54fb053",
                    "moviepass.helpshift.com",
                    "moviepass_platform_20170512180003329-05097f788df2b3a",
                    installConfig);
            if (UserPreferences.INSTANCE.getUserId() != 0) {
                Core.login(HelpshiftHelper.Companion.getHelpshiftUser());
            }
        } catch (InstallException e) {
        }


    }

    protected void inject() {
        DagagerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}


