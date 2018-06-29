package com.mobile.home

import android.os.Build
import android.util.Log
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.UserPreferences.setRestrictions
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


class HomeActivityPresenter(val view: HomeActivityView, val api: Api, val microApi: MicroApi, val sessionManager: SessionManager, val restrictionManager: RestrictionsManager) {

    var androidIdDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var deviceId: String? = null
    var lastRestrictionRequest: Long = 0
    var currentReservation: CurrentReservationV2? = null

    fun onDeviceId(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun onResume() {
        if (UserPreferences.getOneDeviceId() == null) {
            checkOneDevice()
        } else {
            checkRestrictions()
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
                    UserPreferences.setOneDeviceId(result.oneDeviceId);
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
                    determineTicketVerification(it)
                    determineAlertScreen(it.alert)
                    determineForceLogout(it)
                    setRestrictions(it)
                    determineActivationScreen(it)
                }, {
                    it.printStackTrace()
                })
    }

    private fun determineActivationScreen(it: MicroServiceRestrictionsResponse?) {
        it ?: return
        when (it.subscriptionStatus) {
            SubscriptionStatus.ACTIVE ->
                if (!UserPreferences.getHasUserSeenCardActivationScreen()) {
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
            UserPreferences.getAlertDisplayedId() -> {
            }
            else -> view.showAlert(it)
        }
    }

    private fun determineTicketVerification(it: MicroServiceRestrictionsResponse) {


        Log.d("reservationID",UserPreferences.getLastReservationPopInfo().toString())

        it.popInfo?.let {
            if (UserPreferences.getLastReservationPopInfo() == 0 ||
                    UserPreferences.getLastReservationPopInfo() != it.reservationId) {
                UserPreferences.saveLastReservationPopInfo(0)
                fetchReservation(it)
            }
        }
    }

    private fun fetchReservation(popInfo: PopInfo) {
        api
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
    }
}