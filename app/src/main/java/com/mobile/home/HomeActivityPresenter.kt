package com.mobile.home

import android.os.Build
import com.mobile.ApiError
import com.mobile.Change
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.deeplinks.DeepLinksManager
import com.mobile.history.HistoryManager
import com.mobile.history.model.ReservationHistory
import com.mobile.model.Alert
import com.mobile.model.Movie
import com.mobile.model.PopInfo
import com.mobile.model.Theater
import com.mobile.network.Api
import com.mobile.network.MicroApi
import com.mobile.reservation.CurrentReservationV2
import com.mobile.responses.AndroidIDVerificationResponse
import com.mobile.responses.RestrictionsResponse
import com.mobile.responses.SubscriptionStatus
import com.mobile.session.SessionManager
import com.mobile.session.UserManager
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class HomeActivityPresenter(val view: HomeActivityView,
                            val api: Api,
                            val microApi: MicroApi,
                            val sessionManager: SessionManager,
                            val restrictionManager: RestrictionsManager,
                            val historyManager: HistoryManager,
                            val analyticsManager: AnalyticsManager,
                            val deepLinksManager: DeepLinksManager,
                            val userManager: UserManager) {

    var androidIdDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var userInfoDisposable: Disposable? = null
    var reservationDisposable: Disposable? = null
    var deviceId: String? = null
    var lastRestrictionRequest: Long = 0
    var currentReservation: CurrentReservationV2? = null
    var changesDisposable: Disposable? = null
    var deepLInksDisposable: Disposable? = null
    var historyDispose: Disposable? = null
    var didShowAlert: Boolean = false
    var theater: Theater? = null
    var movie: Movie? = null


    fun onDeviceId(deviceId: String?) {
        this.deviceId = deviceId

    }

    fun onCreate() {
        latestMovieWithoutRating()
        listenForDeepLink()
    }


    fun onResume() {
        if (UserPreferences.oneDeviceId == null) {
            checkOneDevice()
        } else {
            checkRestrictions()
        }
        setProfileTabBadge()
        subscribeToBadgeChange()

        if (UserPreferences.loadUserData) {
            retrieveUserData()
        }
    }

    private fun subscribeToBadgeChange() {
        changesDisposable?.dispose()
        changesDisposable = UserPreferences.changes()
                .filter { it.first == Change.PEEK_PASS_BADGE_CHANGE }
                .subscribe {
                    setProfileTabBadge()
                }
    }

    private fun listenForDeepLink() {
        deepLInksDisposable?.dispose()
        deepLInksDisposable = deepLinksManager
                .subScribeToDeepLink()
                .map {
                    movie = it.movie
                    theater = it.theater
                }.subscribe({
                    if (theater != null) {
                        view.updateBottomNavForTheaters()
                    } else if (movie != null) {
                        view.updateBottomNavForMovies()
                    }

                }, {
                    it.printStackTrace()
                })
    }

    private fun setProfileTabBadge() {
        val badge = UserPreferences.showPeakPassBadge
        when (badge) {
            true -> view.showPeakPassBadge()
            false -> view.hidePeakPassBadge()
        }
    }

    private fun checkOneDevice() {
        androidIdDisposable?.dispose()

        val userId = sessionManager.getUser()?.id?.toString() ?: return
        val deviceId = this.deviceId ?: return
        val device = "ANDROID"
        val deviceType = Build.DEVICE ?: return
        val request = AndroidIDVerificationResponse(device, deviceId, deviceType, true)
        androidIdDisposable = api
                .verifyAndroidIDRx(
                        userId,
                        request
                )
                .subscribe({ result ->
                    UserPreferences.oneDeviceId = result.oneDeviceId;
                    checkRestrictions()
                }, { error ->
                    if (error is ApiError) {
                        if (error.httpErrorCode == 403) {
                            analyticsManager.onUserLoggedOut(sessionManager.getUser())
                            sessionManager.logout()
                            view.logout()
                        } else {
                            checkRestrictions()
                        }
                    }
                })
    }

    private fun checkRestrictions() {
        val userId = sessionManager.getUser()?.id ?: return
        val diff = System.currentTimeMillis() - lastRestrictionRequest
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
        when (minutes < 1) {
            true -> return
        }
        restrictionsDisposable?.dispose()
        restrictionsDisposable = microApi.getSession(userId)
                .subscribe({
                    lastRestrictionRequest = System.currentTimeMillis()
                    restrictionManager.publish(it)
                    determineShowWhiteListFragment(it)
                    determineActivationScreen(it)
                    determineForceLogout(it)
                    UserPreferences.restrictions = it
                    determineOverSoftCap(it)
                    determineTicketVerification(it)
                    determineAlertScreen(it.alert)
                    analyticsManager.onBrazeDataSetUp(UserPreferences.user)
                }, {
                    it.printStackTrace()
                })
    }

    private fun retrieveUserData() {
        userInfoDisposable?.dispose()
        userInfoDisposable = userManager
                .getUserInfo()
                .doOnSuccess { UserPreferences.loadUserData = false }
                .map {
                    UserPreferences.userInfo = it
                    println("USER INFO IS" + UserPreferences.userInfo)
                    println("USER INFO IS" + it.shippingAddressLine1)
                    println("USER INFO IS" + UserPreferences.userInfo.shippingAddressLine1)
                    UserPreferences.user = it.user ?: return@map
                }
                .subscribe({
                }, {
                    it.printStackTrace()
                })
    }

    private fun determineShowWhiteListFragment(it: RestrictionsResponse?) {
        val restrictions = it ?: return
        val showMovieListedFragment = UserPreferences.showWhiteListPopUp
        when {
            showMovieListedFragment && restrictions.capWhitelistedMovieIds.isNotEmpty() -> {
                UserPreferences.showWhiteListPopUp = false
                view.showWhiteListMovie()
            }
        }
    }

    private fun determineOverSoftCap(it: RestrictionsResponse) {
        when {
            it.cappedPlan?.isOverSoftCap == true && it.cappedPlan?.used == 3 -> {
                view.showOverSoftCap()
            }
        }
    }

    fun latestMovieWithoutRating() {
        historyDispose?.dispose()
        historyDispose =
                historyManager
                        .fetchLastMovieWithoutRating()
                        .subscribe({ r ->
                            showMovieForRating(r)
                        }) {
                            it.printStackTrace()
                        }
    }

    private fun showMovieForRating(movie: ReservationHistory) {
        when (movie.userRating) {
            null ->
                movie.id?.let {
                    if (it != UserPreferences.historyRatingAlertId) {
                        view.showHistoryRateScreen(movie)
                        UserPreferences.setUserDismissedHistoryRateScreen(it)
                    }
                }
            else -> return
        }
    }


    private fun determineActivationScreen(it: RestrictionsResponse?) {
        it ?: return
        when (it.subscriptionStatus) {
            SubscriptionStatus.ACTIVE ->
                if (!UserPreferences.hasUserSeenCardActivationScreen) {
                    view.showActivatedCardScreen()
                }
            else -> return
        }

    }

    private fun determineForceLogout(it: RestrictionsResponse) {
        if (it.logoutInfo?.isForceLogout == false) {
            return
        }
        sessionManager.logout()
        val logout = it.logoutInfo ?: return
        view.showForceLogout(logout)
    }

    private fun determineAlertScreen(it: Alert?) {
        it ?: return
        if (didShowAlert) {
            return
        }
        when (it.id) {
            UserPreferences.alertDisplayedId -> {
            }
            else -> {
                didShowAlert = true
                view.showAlert(it)
            }
        }
    }

    private fun determineTicketVerification(it: RestrictionsResponse) {
        val popInfo = it.popInfo ?: return
        if (UserPreferences.lastReservationPopInfo == 0 ||
                UserPreferences.lastReservationPopInfo != popInfo.reservationId) {
            UserPreferences.saveLastReservationPopInfo(0)
            fetchReservation(popInfo)
        }
    }

    private fun fetchReservation(popInfo: PopInfo) {
        reservationDisposable?.dispose()
        reservationDisposable = api
                .lastReservation()
                .subscribe({
                    currentReservation = it
                    view.showConfirmationScreen(it)
                }
                        , {
                    view.showTicketVerification(popInfo)
                })
    }

    fun onDestroy() {
        androidIdDisposable?.dispose()
        restrictionsDisposable?.dispose()
        reservationDisposable?.dispose()
    }

    fun onPause() {
        androidIdDisposable?.dispose()
        restrictionsDisposable?.dispose()
        changesDisposable?.dispose()
        historyDispose?.dispose()
    }
}