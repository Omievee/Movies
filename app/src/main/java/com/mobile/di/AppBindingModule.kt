package com.mobile.di

import com.mobile.billing.MissingBillingFragment
import com.mobile.billing.MissingBillingFragmentModule
import com.mobile.activities.ActivateMoviePassCard
import com.mobile.fragments.*
import com.mobile.history.HistoryDetailsFragment
import com.mobile.history.PastReservationsFragment
import com.mobile.home.HomeActivity
import com.mobile.home.HomeActivityModule
import com.mobile.reservation.CheckInFragment
import com.mobile.reservation.ReservationActivity
import com.mobile.reservation.ReservationCheckinModule
import com.mobile.seats.BringAFriendActivity
import com.mobile.seats.ConfirmDetailsFragment
import com.mobile.splash.SplashActivity
import com.mobile.splash.SplashActivityModule
import com.mobile.theater.TheaterMapFragment
import com.mobile.theater.TheatersFragmentModule
import com.mobile.ticketverification.OcrCaptureFragment
import com.mobile.ticketverification.TicketVerificationBottomSheetDialogFragment
import com.mobile.surge.ConfirmSurgeFragment
import com.mobile.tv.ReservationActivityModule
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
    @ContributesAndroidInjector(modules = [TheatersFragmentModule::class])
    fun theatersFragment(): TheatersFragmentV2

    @FragmentScope
    @ContributesAndroidInjector
    fun theaterFragmentV2(): ScreeningsFragment

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

    @TVScope
    @ContributesAndroidInjector(modules = [TicketVerificationBottomSheetModule::class])
    fun ticketVerificationBottomSheetDialogFragment(): TicketVerificationBottomSheetDialogFragment

    @TVScope
    @ContributesAndroidInjector()
    fun ocrCaptureFragment():OcrCaptureFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun theaterMap(): TheaterMapFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ReservationActivityModule::class])
    fun reservationActivity(): ReservationActivity

    @ActivityScope
    @ContributesAndroidInjector
    fun activateMoviepassCardActivity(): ActivateMoviePassCard

    @ActivityScope
    @ContributesAndroidInjector
    fun profileAccountCancellation(): ProfileCancellationFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [MissingBillingFragmentModule::class])
    fun missingBilling(): MissingBillingFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ReservationCheckinModule::class])
    fun checkinFragment(): CheckInFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun confirmSurgeFragment(): ConfirmSurgeFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun ticketVerification(): TicketVerificationV2
}