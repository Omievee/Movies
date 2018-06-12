package com.mobile.di

import com.mobile.home.HomeActivity
import com.mobile.home.HomeActivityModule
import com.mobile.splash.SplashActivity
import com.mobile.splash.SplashActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AppBindingModule {

    /**
     * Generates boilerplate
     */
    @ActivityScope
    @ContributesAndroidInjector(modules=[SplashActivityModule::class])
    abstract fun splashActivity() : SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules=[HomeActivityModule::class])
    abstract fun homeActivity() : HomeActivity

}