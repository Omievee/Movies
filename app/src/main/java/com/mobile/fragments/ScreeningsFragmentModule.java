package com.mobile.fragments;

import com.mobile.analytics.AnalyticsManager;
import com.mobile.di.FragmentScope;
import com.mobile.history.HistoryManager;
import com.mobile.location.LocationManager;
import com.mobile.movie.MoviesManager;
import com.mobile.network.Api;

import dagger.Module;
import dagger.Provides;

@Module
public class ScreeningsFragmentModule {

    @Provides
    @FragmentScope
    ScreeningsFragmentPresenter providePresenter(ScreeningsFragment fragment, Api api, LocationManager locationManager, HistoryManager historyManager, AnalyticsManager analyticsManager, MoviesManager moviesManager) {
        return new ScreeningsFragmentPresenter(fragment, (ScreeningsData)fragment.getArguments().getParcelable("data"), api, locationManager, historyManager, analyticsManager, moviesManager);
    }
}
