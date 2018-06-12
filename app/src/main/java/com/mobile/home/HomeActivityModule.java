package com.mobile.home;

import com.mobile.di.ActivityScope;
import com.mobile.location.LocationManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class HomeActivityModule {

    @Provides
    @ActivityScope
    static HomeActivityPresenter provideLoginPresenter(LocationManager manager) {
        return new HomeActivityPresenter();
    }

}