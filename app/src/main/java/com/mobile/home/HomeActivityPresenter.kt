package com.mobile.home

import android.os.Build
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.UserPreferences.setRestrictions
import com.mobile.model.Alert
import com.mobile.model.Reservation
import com.mobile.model.Screening
import com.mobile.model.ScreeningToken
import com.mobile.network.Api
import com.mobile.network.MicroApi
import com.mobile.network.RestClient
import com.mobile.session.SessionManager
import io.reactivex.disposables.Disposable
import com.mobile.responses.AndroidIDVerificationResponse
import com.mobile.responses.ETicketConfirmation
import com.mobile.responses.MicroServiceRestrictionsResponse
import java.text.SimpleDateFormat


class HomeActivityPresenter(val view: HomeActivityView, val api: Api, val microApi:MicroApi, val sessionManager: SessionManager) {

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
        restrictionsDisposable = microApi.getSession(userId)
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
        if (it.logoutInfo?.isForceLogout == false) {
            return
        }
        sessionManager.logout()
        val logout = it.logoutInfo?:return
        view.showForceLogout(logout)
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
        if (oldStatus != newStatus.toString()) {
            view.showSubscriptionButton(it)
        }
    }

    private fun determineTicketVerification(it: MicroServiceRestrictionsResponse) {
        it.popInfo?.let {
            if(!showReservation())
                view.showTicketVerification(it)
        }
    }

    private fun showReservation(): Boolean {
        var reservationAvailable: Boolean? = false
        RestClient
                .getAuthenticated()
                .lastReservation()
                .subscribe({
                    val screening = Screening.from(it)
                    var confirmation: ETicketConfirmation? = null
                    if (it.ticket != null) {
                        confirmation = ETicketConfirmation()
                        confirmation.confirmationCode = it.ticket!!.redemptionCode
                        confirmation.barCodeUrl = ""
                    }
                    var reservation: Reservation? = null
                    if (it.reservation != null) {
                        reservation = Reservation()
                        reservation.id = it.reservation!!.id!!
                    }
                    val token = ScreeningToken(
                            screening,
                            SimpleDateFormat("h:mm a").format(it.showtime),
                            reservation,
                            confirmation,
                            null
                    )
                    view.showConfirmationScreen(token)
                    reservationAvailable = true
                }
                        ,{

                })
        return reservationAvailable ?: false
    }

    fun onDestroy() {
        androidIdDisposable?.dispose()
        restrictionsDisposable?.dispose()
    }
}