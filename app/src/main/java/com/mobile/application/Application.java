package com.mobile.application;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

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
import com.mobile.helpers.RealmTaskService;
import com.mobile.helpshift.HelpshiftHelper;
import com.mobile.network.RestClient;
import com.mobile.utils.FastStack;
import com.taplytics.sdk.Taplytics;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Application extends MultiDexApplication implements HasActivityInjector {

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

    @Nullable public Activity getCurrentActivity() {
        return activityStack.peek();
    }

    public FastStack<Activity> getActivityStack() {
        return activityStack;
    }

    //giguyigfyug
    @Override
    public void onCreate() {
        super.onCreate();
        inject();
        UserPreferences.INSTANCE.load(this, gson);
        Taplytics.startTaplytics(this, "3629c653bc0ece073faa45be6fa7081561426e87");
        Fabric.with(this, new Crashlytics());
        Fresco.initialize(this);
        RealmTaskService.scheduleRepeatTask(this);
        RealmTaskService.scheduleRepeatTaskTheaters(this);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().name(Realm.DEFAULT_REALM_NAME).build();
        Realm.setDefaultConfiguration(config);
        RestClient.setupAuthenticatedWebClient(getApplicationContext());
        RestClient.setupAuthenticatedGoWatchIt(getApplicationContext());
        RestClient.setUpLocalStorage(getApplicationContext());
        RestClient.setUpRegistration(getApplicationContext());
        RestClient.setupMicroService(getApplicationContext());
        InstallConfig installConfig = new InstallConfig.Builder().build();
        Core.init(All.getInstance());
        registerActivityLifecycleCallbacks(new ActivigtyCallbacks() {
            @Override
            public void onActivityResumed(@Nullable Activity activity) {
                activityStack.push(activity);
                if(firstActivity==null) {
                    firstActivity = activity;
                    analyticsManager.onAppOpened();
                }
            }

            @Override
            public void onActivityDestroyed(@Nullable Activity activity) {
                if(firstActivity==activity) {
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

            Core.login(HelpshiftHelper.Companion.getHelpshiftUser());
        } catch (InstallException e) {
        }
    }

    protected void inject() {
        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}


