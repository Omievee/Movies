package com.mobile.history;

import com.mobile.di.ActivityScope;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class HistoryDetailModule {
    @Provides
    @ActivityScope
    static HistoryDetailPresenter provideHistoryDetailPresenter(HistoryManager historyManager) {
        return new HistoryDetailPresenter(historyManager);
    }
}
