package com.mobile.fragments;

import com.mobile.di.ActivityScope;
import com.mobile.di.FragmentScope;
import com.mobile.home.HomeActivityPresenter;
import com.mobile.location.LocationManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MoviesFragmentModule {

    @Provides
    @FragmentScope
    static MoviesFragmentPresenter provideLoginPresenter(MoviesFragment fragment) {
        return new MoviesFragmentPresenter(fragment);
    }

}