package com.mobile.tv;

import com.mobile.di.ActivityScope;
import com.mobile.network.Api;
import com.mobile.reservation.ReservationActivity;
import com.mobile.reservation.ReservationActivityPresenter;
import com.mobile.reservation.ReservationView;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class ReservationActivityModule {

    @Provides
    @ActivityScope
    static ReservationActivityPresenter provideLoginPresenter(ReservationActivity activity, Api api) {
        return new ReservationActivityPresenter(activity, api);
    }
}