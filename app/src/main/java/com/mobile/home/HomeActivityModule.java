package com.mobile.home;

import com.mobile.di.ActivityScope;
import com.mobile.location.LocationManager;
import com.mobile.network.Api;
import com.mobile.network.MicroApi;
import com.mobile.session.SessionManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class HomeActivityModule {

    @Provides
    @ActivityScope
    static HomeActivityPresenter provideLoginPresenter(HomeActivity activity, LocationManager manager, Api api, MicroApi micro, SessionManager sessionManager) {
        return new HomeActivityPresenter(activity, api, micro, sessionManager);
    }

}