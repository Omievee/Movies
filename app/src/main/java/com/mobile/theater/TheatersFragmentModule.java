package com.mobile.theater;

import com.mobile.analytics.AnalyticsManager;
import com.mobile.di.FragmentScope;
import com.mobile.fragments.TheatersFragmentPresenter;
import com.mobile.fragments.TheatersFragmentV2;
import com.mobile.location.Geocoder;
import com.mobile.location.LocationManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class TheatersFragmentModule {

    @Provides
    @FragmentScope
    static TheatersFragmentPresenter providePresenter(TheatersFragmentV2 fragment, LocationManager locationManager, TheaterManager manager, TheaterUIManager uiManager, Geocoder geocoder, AnalyticsManager analyticsManager) {
        return new TheatersFragmentPresenter(fragment, locationManager, manager, uiManager, geocoder, analyticsManager);
    }
}
