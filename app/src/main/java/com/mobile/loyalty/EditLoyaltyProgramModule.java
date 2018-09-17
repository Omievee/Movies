package com.mobile.loyalty;


import com.mobile.di.FragmentScope;
import com.mobile.network.Api;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class EditLoyaltyProgramModule {


    @Provides
    @FragmentScope
    static EditLoyaltyProgramPresenter provideEditPresenter(EditLoyaltyProgramFragment fragment, Api api) {
        return new EditLoyaltyProgramPresenter(fragment, api);
    }

}
