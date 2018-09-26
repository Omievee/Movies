package com.mobile.home;

import com.mobile.analytics.AnalyticsManager;
import com.mobile.deeplinks.DeepLinksManager;
import com.mobile.di.ActivityScope;
import com.mobile.history.HistoryManager;
import com.mobile.network.Api;
import com.mobile.network.MicroApi;
import com.mobile.session.SessionManager;
import com.mobile.session.UserManager;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class HomeActivityModule {

    @Provides
    @ActivityScope
    static HomeActivityPresenter provideLoginPresenter(HomeActivity activity, Api api, MicroApi micro, SessionManager sessionManager, RestrictionsManager restrictionsManager, HistoryManager historyManager, AnalyticsManager analyticsManager, DeepLinksManager deepLinksManager, UserManager userManager) {
        return new HomeActivityPresenter(activity, api, micro, sessionManager, restrictionsManager, historyManager, analyticsManager, deepLinksManager,  userManager);
    }
}