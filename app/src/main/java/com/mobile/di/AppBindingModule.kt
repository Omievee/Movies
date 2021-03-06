package com.mobile.di

import com.mobile.activities.ActivateMoviePassCard
import com.mobile.activities.LogInActivity
import com.mobile.alertscreen.AlertScreenFragment
import com.mobile.alertscreen.AlertScreenModule
import com.mobile.billing.ChangeBillingAndPlanInfoFragment
import com.mobile.profile.ChangeShippingAddress
import com.mobile.billing.MissingBillingFragment
import com.mobile.billing.MissingBillingFragmentModule
import com.mobile.fragments.*
import com.mobile.home.HomeActivity
import com.mobile.home.HomeActivityModule
import com.mobile.loyalty.EditLoyaltyProgramFragment
import com.mobile.loyalty.EditLoyaltyProgramModule
import com.mobile.movie.MoviesFragment
import com.mobile.profile.*
import com.mobile.referafriend.ReferAFriendFragment
import com.mobile.referafriend.ReferAFriendFragmentModule
import com.mobile.reservation.CheckInFragment
import com.mobile.reservation.ReservationActivity
import com.mobile.reservation.ReservationCheckinModule
import com.mobile.seats.BringAFriendActivity
import com.mobile.seats.ConfirmDetailsFragment
import com.mobile.splash.SplashActivity
import com.mobile.splash.SplashActivityModule
import com.mobile.surge.ConfirmSurgeFragment
import com.mobile.theater.TheaterMapFragment
import com.mobile.theater.TheatersFragmentModule
import com.mobile.ticketverification.OcrCaptureFragment
import com.mobile.ticketverification.TicketVerificationBottomSheetDialogFragment
import com.mobile.tv.ReservationActivityModule
import dagger.Module
import com.mobile.history.*
import com.mobile.plans.ChangePlansFragment
import com.mobile.plans.ChangePlansModule
import dagger.android.ContributesAndroidInjector

@Module
interface AppBindingModule {

    /**
     * Generates boilerplate
     */

    @ActivityScope
    @ContributesAndroidInjector
    fun loginActivity(): LogInActivity

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
    fun searchFragment(): SearchFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TheatersFragmentModule::class])
    fun theatersFragment(): TheatersFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ScreeningsFragmentModule::class])
    fun theaterFragmentV2(): ScreeningsFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun bringAFriendActivity(): BringAFriendActivity

    @FragmentScope
    @ContributesAndroidInjector
    fun confirmDetailsFragment(): ConfirmDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun pastReservationsFragment(): HistoryFragment

    @TVScope
    @ContributesAndroidInjector(modules = [TicketVerificationBottomSheetModule::class])
    fun ticketVerificationBottomSheetDialogFragment(): TicketVerificationBottomSheetDialogFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun enableLocation(): EnableLocationFragment

    @TVScope
    @ContributesAndroidInjector()
    fun ocrCaptureFragment(): OcrCaptureFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun theaterMap(): TheaterMapFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ReservationActivityModule::class])
    fun reservationActivity(): ReservationActivity

    @ActivityScope
    @ContributesAndroidInjector
    fun activateMoviepassCardActivity(): ActivateMoviePassCard

    @FragmentScope
    @ContributesAndroidInjector
    fun changeBilling(): ChangeBillingAndPlanInfoFragment

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
    fun changeEmail(): ChangeEmail

    @FragmentScope
    @ContributesAndroidInjector
    fun profileFragmentt(): ProfileFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun ticketVerification(): TicketVerificationV2

    @FragmentScope
    @ContributesAndroidInjector(modules = [ProfileCancellationModule::class])
    fun profileCancellationFragment(): ProfileCancellationFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ReferAFriendFragmentModule::class])
    fun referAFriendFragment(): ReferAFriendFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [EditLoyaltyProgramModule::class])
    fun editLoyaltyFragment(): EditLoyaltyProgramFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun accountDetailsFragment(): AccountDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector
    fun accountInformation(): AccountInformation

    @FragmentScope
    @ContributesAndroidInjector
    fun changePassword(): ChangePassword

    @FragmentScope
    @ContributesAndroidInjector
    fun changeShippingAddress(): ChangeShippingAddress


    @FragmentScope
    @ContributesAndroidInjector(modules = [AlertScreenModule::class])
    fun alertScreenFragment(): AlertScreenFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [HistoryDetailModule::class])
    fun historyDetailActivity(): HistoryDetailsActivity


    @FragmentScope
    @ContributesAndroidInjector(modules = [ChangePlansModule::class])
    fun changePlansFragment(): ChangePlansFragment
}