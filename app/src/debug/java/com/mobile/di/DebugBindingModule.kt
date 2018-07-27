package com.mobile.di

import com.moviepass.debug.DebugActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface DebugBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    fun debugActivity():DebugActivity

}
