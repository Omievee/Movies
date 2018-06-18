package com.mobile.di

import com.mobile.fragments.*
import com.mobile.history.PastReservationsFragment
import com.mobile.home.HomeActivity
import com.mobile.home.HomeActivityModule
import com.mobile.seats.BringAFriendActivity
import com.mobile.seats.ConfirmDetailsFragment
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
    @ContributesAndroidInjector(modules = [SplashActivityModule::class])
    abstract fun splashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeActivityModule::class])
    abstract fun homeActivity(): HomeActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [MoviesFragmentModule::class])
    abstract fun moviesFragment(): MoviesFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun theatersFragment(): TheatersFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun theaterFragment(): TheaterFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun movieFragment(): MovieFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bringAFriendActivity(): BringAFriendActivity

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun confirmDetailsFragment(): ConfirmDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun pastReservationsFragment(): PastReservationsFragment


}