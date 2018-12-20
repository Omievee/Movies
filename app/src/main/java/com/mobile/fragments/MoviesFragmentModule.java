package com.mobile.fragments;

import android.content.Context;

import com.mobile.application.Application;
import com.mobile.deeplinks.DeepLinksManager;
import com.mobile.di.FragmentScope;
import com.mobile.home.RestrictionsManager;
import com.mobile.location.LocationManager;
import com.mobile.movie.MoviesFragment;
import com.mobile.movie.MoviesManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MoviesFragmentModule {

    @Provides
    @FragmentScope
    static MoviesFragmentPresenter provideLoginPresenter(Application context, MoviesFragment fragment, MoviesManager manager, RestrictionsManager restrictionsManager, DeepLinksManager deepLinksManager, LocationManager locationManager) {
        return new MoviesFragmentPresenter(context, fragment, manager, restrictionsManager, deepLinksManager, locationManager);
    }

}