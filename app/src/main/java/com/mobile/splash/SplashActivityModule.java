package com.mobile.splash;

import com.mobile.di.ActivityScope;
import com.mobile.history.HistoryManager;
import com.mobile.network.Api;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class SplashActivityModule {

    @Provides
    @ActivityScope
    static SplashActivityPresenter provideLoginPresenter(HistoryManager historyManager, Api api) {
        return new SplashActivityPresenter(historyManager);
    }

}