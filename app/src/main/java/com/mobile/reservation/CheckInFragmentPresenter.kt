package com.mobile.reservation

import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.location.LocationManager
import com.mobile.model.SurgeType
import com.mobile.model.TicketType
import com.mobile.network.SurgeResponse
import com.mobile.requests.TicketInfoRequest
import com.mobile.responses.SubscriptionStatus
import com.mobile.tickets.TicketManager
import com.mobile.utils.text.centsAsDollars
import io.reactivex.disposables.Disposable

class CheckInFragmentPresenter(val view: CheckInFragmentView, val api: TicketManager, val locationManager: LocationManager, val analyticsManager: AnalyticsManager) {

    var checkin: Checkin? = null
    var surgeCheckDis: Disposable? = null
    var reservDis: Disposable? = null

    fun onCreate(checkin: Checkin?) {
        this.checkin = checkin

        display()
    }

    private fun display() {
        val check: Checkin = checkin ?: return
        when (check.availability.isETicket()) {
            true -> doEticketing()
            false -> doSurge()
        }
    }

    private val showActivateCard: Boolean
        get() {
            return when (UserPreferences.restrictions.subscriptionStatus) {
                SubscriptionStatus.PENDING_ACTIVATION,
                SubscriptionStatus.PENDING_FREE_TRIAL -> when (checkin?.availability?.ticketType) {
                    TicketType.STANDARD -> true
                    else -> false
                }
                else -> false
            }
        }

    private val showProofOfPurchase: Boolean
        get() {
            return checkin?.screening?.popRequired == true || UserPreferences.restrictions.proofOfPurchaseRequired
        }


    private fun doEticketing() {
        view.showContinueToETicketing()
    }

    private fun doSurge() {
        val surge = checkin?.screening?.getSurge(checkin?.availability?.startTime, UserPreferences.restrictions.userSegments)
                ?: return
        val peak = UserPreferences.restrictions.peakPassInfo
        when (surge.level) {
            SurgeType.NO_SURGE -> {
                view.showCheckin()
                if (showProofOfPurchase) {
                    view.showCheckinWithProof()
                }
            }
            SurgeType.WILL_SURGE -> {
                view.showWillSurge(surge, peak, peak.currentPeakPass)
                if (showProofOfPurchase) {
                    view.showCheckinWithProof()
                }
            }
            else -> view.showSurge(surge, peak, peak.currentPeakPass)
        }
    }

    fun onContinueToETicketingClicked() {
        val checkin = checkin ?: return
        view.navigateToCreateReservation(checkin)
    }

    fun onContinueClicked() {
        surgeCheckDis?.dispose()
        val checkin = checkin ?: return
        val payload = checkin.availability.providerInfo ?: return

        if (showActivateCard) {
            return view.showActivateCard(checkin)
        }

        val surge = checkin.screening.getSurge(checkin.availability.startTime, UserPreferences.restrictions.userSegments)
        when (surge.level) {
            SurgeType.SURGING -> {
                val peakPass = UserPreferences.restrictions.peakPassInfo.currentPeakPass
                return when (peakPass) {
                    null -> view.navigateToSurchargeConfirm(checkin)
                    else -> view.showApplyPeakPass(checkin, UserPreferences.restrictions.peakPassInfo, UserPreferences.restrictions.peakPassInfo.currentPeakPass)
                }

            }
            else -> {
            }
        }
        view.showProgress()
        surgeCheckDis = api.peakCheck(
                payload
        ).doAfterTerminate {
            view.hideProgress()
        }
                .subscribe({
                    onSurgeResponse(it)
                }, {
                    val apiError: ApiError = it as? ApiError
                            ?: return@subscribe view.showGenericError()
                    view.showError(apiError)
                })
    }

    private fun onSurgeResponse(it: SurgeResponse) {
        val checkin = checkin ?: return
        when (it.currentlyPeaking) {
            true -> {
                checkin.screening.updateSurge(checkin.availability, it)
                val surge = checkin.getSurge(UserPreferences.restrictions.userSegments)
                val peak = UserPreferences.restrictions.peakPassInfo
                val peakPass = peak.currentPeakPass
                when (peak.enabled) {
                    false -> showSurgeModal(it)
                    true -> when (peakPass) {
                        null -> view.showNowPeakingNoPeakPass(checkin, surge)
                        else -> view.showNowPeakingApplyPeakPass(it, peak, peakPass)
                    }
                }

            }
            false -> createReservation()
        }
    }

    private fun showSurgeModal(surge: SurgeResponse) {
        val message = surge.peakMessage
        when (message) {
            null -> view.showSurgeModal(surge.peakAmount.centsAsDollars)
            else -> view.showSurgeModal(message)
        }
    }

    private fun showSurgeModal(text: String) {
        view.showSurgeModal(text)
    }

    private fun createReservation() {
        val checkin = checkin ?: return
        val perf = checkin.availability.providerInfo ?: return
        val loc = locationManager.lastLocation() ?: return view.showNeedLocation()
        reservDis?.dispose()
        view.showProgress()
        reservDis = api
                .reserve(checkin, TicketInfoRequest(
                        performanceInfo = perf,
                        latitude = loc.lat,
                        longitude = loc.lon
                ))
                .doOnSubscribe {
                    analyticsManager.onCheckinAttempt(checkin)
                }
                .subscribe({
                    view.navigateTo(checkin, it)
                    analyticsManager.onCheckinSuccessful(checkin, it)
                }, {
                    val apiError = it as? ApiError ?: return@subscribe
                    view.showError(apiError)
                    analyticsManager.onCheckinFailed(checkin)
                })
    }

    fun onDestroy() {
        surgeCheckDis?.dispose()
        reservDis?.dispose()
    }

    fun onContinueDialogClicked() {
        val chekcin = checkin ?: return
        view.navigateToSurchargeConfirm(chekcin)
    }

    fun onPeakPassInfoClicked() {
        val checkin = checkin ?: return
        val peak = UserPreferences.restrictions.peakPassInfo
        val peakPass = peak.currentPeakPass
        view.showPeakPassSheet(checkin, peak, peakPass)
    }

    fun onApplyPeakPassClicked() {
        val checkin = checkin ?: return
        checkin.peakPass = UserPreferences.restrictions.peakPassInfo.currentPeakPass
        view.navigateToSurchargeConfirm(checkin)
    }

    fun onSavePeakPassForLaterClicked() {
        val checkin = checkin ?: return
        checkin.peakPass = null
        view.navigateToSurchargeConfirm(checkin)
    }

}
