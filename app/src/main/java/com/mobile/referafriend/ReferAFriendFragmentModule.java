package com.mobile.referafriend;

import com.mobile.di.FragmentScope;
import com.mobile.network.Api;

import dagger.Module;
import dagger.Provides;

@Module
public class ReferAFriendFragmentModule {

    @Provides
    @FragmentScope
    static ReferAFriendFragmentPresenter provideReferAFriendFragmentPresenter(Api api, ReferAFriendFragment fragment) {
        return new ReferAFriendFragmentPresenter(api, fragment);
    }
}