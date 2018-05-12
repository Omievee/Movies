package com.mobile.activities;

import com.mobile.di.ActivityScope;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class SplashActivityModule {

    @Provides
    @ActivityScope
    static SplashActivityPresenter provideLoginPresenter() {
        return new SplashActivityPresenter();
    }

}