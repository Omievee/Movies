package com.mobile.plans;

import com.mobile.di.FragmentScope;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class ChangePlansModule {


    @Provides
    @FragmentScope
    static ChangePlansPresenter providePresenter(ChangePlansFragment fragment, PlansManager plansManager) {
        return new ChangePlansPresenter(fragment, plansManager);
    }

}
