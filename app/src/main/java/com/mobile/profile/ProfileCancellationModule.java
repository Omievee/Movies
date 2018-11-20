package com.mobile.profile;

import com.mobile.di.FragmentScope;
import com.mobile.network.Api;
import com.mobile.network.BillingApi;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class ProfileCancellationModule {

    @Provides
    @FragmentScope
    static ProfileCancellationPresenter provideLoginPresenter(Api api, BillingApi billingApi, ProfileCancellationFragment fragment) {
        return new ProfileCancellationPresenter(api, billingApi, fragment);
    }
}
