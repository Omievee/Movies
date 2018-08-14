package com.mobile.alertscreen;

import com.mobile.di.FragmentScope;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class AlertScreenModule {


    @Provides
    @FragmentScope
    static AlertScreenPresenter provideAlertScreenPresenter(AlertScreenFragment fragment) {
        return new AlertScreenPresenter(fragment);
    }

}
