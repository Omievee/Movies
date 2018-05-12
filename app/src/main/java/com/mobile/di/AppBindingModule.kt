package com.mobile.di

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

}