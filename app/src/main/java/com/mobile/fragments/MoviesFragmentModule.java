package com.mobile.fragments;

import com.google.gson.Gson;
import com.mobile.application.Application;
import com.mobile.di.FragmentScope;
import com.mobile.home.RestrictionsManager;
import com.mobile.movie.MoviesFragment;
import com.mobile.movie.MoviesManager;
import com.mobile.movie.MoviesManagerImpl;
import com.mobile.network.StaticApi;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MoviesFragmentModule {

    @Provides
    @FragmentScope
    static MoviesFragmentPresenter provideLoginPresenter(MoviesFragment fragment, MoviesManager manager, RestrictionsManager restrictionsManager) {
        return new MoviesFragmentPresenter(fragment, manager, restrictionsManager);
    }

}