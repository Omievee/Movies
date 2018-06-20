package com.mobile.di

import com.mobile.fragments.*
import com.mobile.history.HistoryDetailsFragment
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
interface AppBindingModule {

    /**
     * Generates boilerplate
     */
    @ActivityScope
    @ContributesAndroidInjector(modules = [SplashActivityModule::class])
    fun splashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeActivityModule::class])
    fun homeActivity(): HomeActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [MoviesFragmentModule::class])
    fun moviesFragment(): MoviesFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun theatersFragment(): TheatersFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun theaterFragment(): TheaterFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun movieFragment(): MovieFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun bringAFriendActivity(): BringAFriendActivity

    @FragmentScope
    @ContributesAndroidInjector
    fun confirmDetailsFragment(): ConfirmDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun pastReservationsFragment(): PastReservationsFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun historyDeatils(): HistoryDetailsFragment


}