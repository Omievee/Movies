package com.mobile.home

import android.os.Build
import com.mobile.ApiError
import com.mobile.Change
import com.mobile.UserPreferences
import com.mobile.history.HistoryManager
import com.mobile.history.model.ReservationHistory
import com.mobile.model.Alert
import com.mobile.model.PopInfo
import com.mobile.network.Api
import com.mobile.network.MicroApi
import com.mobile.reservation.CurrentReservationV2
import com.mobile.responses.AndroidIDVerificationResponse
import com.mobile.responses.MicroServiceRestrictionsResponse
import com.mobile.responses.SubscriptionStatus
import com.mobile.session.SessionManager
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class HomeActivityPresenter(val view: HomeActivityView, val api: Api, val microApi: MicroApi, val sessionManager: SessionManager, val restrictionManager: RestrictionsManager, val historyManager: HistoryManager) {

    var androidIdDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var reservationDisposable: Disposable? = null
    var deviceId: String? = null
    var lastRestrictionRequest: Long = 0
    var currentReservation: CurrentReservationV2? = null
    var changesDisposable: Disposable? = null
    var historyDispose: Disposable? = null

    fun onDeviceId(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun onCreate(){
        latestMovieWithoutRating()
    }

    fun onResume() {
        if (UserPreferences.oneDeviceId == null) {
            checkOneDevice()
        } else {
            checkRestrictions()
        }
        setProfileTabBadge()
        subscribeToBadgeChange()
    }

    private fun subscribeToBadgeChange() {
        changesDisposable?.dispose()
        changesDisposable = UserPreferences.changes()
                .filter { it.first == Change.PEEK_PASS_BADGE_CHANGE }
                .subscribe {
                    setProfileTabBadge()
                }
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
                        if (error.httpErrorCode / 100 == 4) {
                            sessionManager.logout()
                            view.logout()
                        } else {
                            checkRestrictions()
                        }
                    }
                })
    }

    private fun checkRestrictions() {
        restrictionsDisposable?.dispose()
        val userId = sessionManager.getUser()?.id ?: return
        val diff = System.currentTimeMillis() - lastRestrictionRequest
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
        when (minutes < 1) {
            true -> return
        }
        restrictionsDisposable = microApi.getSession(userId)
                .subscribe({
                    lastRestrictionRequest = System.currentTimeMillis()
                    restrictionManager.publish(it)
                    determineActivationScreen(it)
                    determineForceLogout(it)
                    UserPreferences.restrictions = it
                    determineTicketVerification(it)
                    determineAlertScreen(it.alert)
                }, {
                    it.printStackTrace()
                })
    }

    fun latestMovieWithoutRating() {
        historyDispose?.dispose()
        historyDispose =
                historyManager
                        .fetchLastMovieWithoutRating()
                        .subscribe({ r ->
                            showMovieForRating(r)
                        }) {

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


    private fun determineActivationScreen(it: MicroServiceRestrictionsResponse?) {
        it ?: return
        when (it.subscriptionStatus) {
            SubscriptionStatus.ACTIVE ->
                if (!UserPreferences.hasUserSeenCardActivationScreen) {
                    view.showActivatedCardScreen()
                }
            else -> return
        }

    }

    private fun determineForceLogout(it: MicroServiceRestrictionsResponse) {
        if (it.logoutInfo?.isForceLogout == false) {
            return
        }
        sessionManager.logout()
        val logout = it.logoutInfo ?: return
        view.showForceLogout(logout)
    }

    private fun determineAlertScreen(it: Alert?) {
        it ?: return
        when (it.id) {
            UserPreferences.alertDisplayedId -> {
            }
            else -> view.showAlert(it)
        }
    }

    private fun determineTicketVerification(it: MicroServiceRestrictionsResponse) {
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