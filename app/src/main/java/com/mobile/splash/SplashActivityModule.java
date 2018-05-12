package com.mobile.splash;

import com.mobile.di.ActivityScope;

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