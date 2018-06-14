package com.mobile.home

import android.os.Build
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.UserPreferences.setRestrictions
import com.mobile.model.Alert
import com.mobile.network.Api
import com.mobile.session.SessionManager
import io.reactivex.disposables.Disposable
import com.mobile.responses.AndroidIDVerificationResponse
import com.mobile.responses.MicroServiceRestrictionsResponse


class HomeActivityPresenter(val view: HomeActivityView, val api: Api, val sessionManager: SessionManager) {

    var androidIdDisposable: Disposable? = null
    var restrictionsDisposable: Disposable? = null
    var deviceId: String? = null

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
        restrictionsDisposable = api.getInterstitialAlertRx(userId + Constants.OFFSET)
                .subscribe({
                    determineShowSnackbar(it)
                    determineTicketVerification(it)
                    determineAlertScreen(it.alert)
                    determineForceLogout(it)
                    setRestrictions(it)
                }, {
                    it.printStackTrace()
                })

    }

    private fun determineForceLogout(it: MicroServiceRestrictionsResponse) {
        if (it.logoutInfo?.isForceLogout == true) {
            return
        }
        sessionManager.logout()
        view.showForceLogout(it.logoutInfo)
    }

    private fun determineAlertScreen(it: Alert?) {
        it ?: return
        when {
            it.id != UserPreferences.getAlertDisplayedId() -> {
                view.showAlert(it)
            }
        }
    }

    private fun determineShowSnackbar(it: MicroServiceRestrictionsResponse) {
        val oldStatus = UserPreferences.getRestrictionSubscriptionStatus()
        val newStatus = it.subscriptionStatus
        if (oldStatus != newStatus) {
            view.showSubscriptionButton(it)
        }
    }

    private fun determineTicketVerification(it: MicroServiceRestrictionsResponse) {
        it.popInfo?.let {
            view.showTicketVerification(it)
        }

    }

    fun onDestroy() {
        androidIdDisposable?.dispose()
        restrictionsDisposable?.dispose()
    }
}