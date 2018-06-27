package com.moviepass.di

import com.mobile.di.ActivityScope
import com.moviepass.debug.DebugActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface DebugBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    fun debugActivity():DebugActivity

}