package com.mobile.fragments;

import com.mobile.deeplinks.DeepLinksManager;
import com.mobile.di.FragmentScope;
import com.mobile.home.RestrictionsManager;
import com.mobile.movie.MoviesFragment;
import com.mobile.movie.MoviesManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MoviesFragmentModule {

    @Provides
    @FragmentScope
    static MoviesFragmentPresenter provideLoginPresenter(MoviesFragment fragment, MoviesManager manager, RestrictionsManager restrictionsManager, DeepLinksManager deepLinksManager) {
        return new MoviesFragmentPresenter(fragment, manager, restrictionsManager, deepLinksManager);
    }

}