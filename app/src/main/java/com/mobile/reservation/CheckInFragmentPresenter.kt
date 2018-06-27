package com.mobile.reservation

import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.helpers.GoWatchItSingleton
import com.mobile.location.LocationManager
import com.mobile.model.SurgeType
import com.mobile.model.TicketType
import com.mobile.model.toSurgeCheck
import com.mobile.network.Api
import com.mobile.network.SurgeResponse
import com.mobile.requests.SurgeCheckRequest
import com.mobile.requests.TicketInfoRequest
import com.mobile.responses.SubscriptionStatus
import com.mobile.tickets.TicketManager
import com.mobile.utils.text.centsAsDollars
import io.reactivex.disposables.Disposable

class CheckInFragmentPresenter(val view: CheckInFragmentView, val api: TicketManager, val locationManager: LocationManager) {

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
            return checkin?.screening?.popRequired == true
        }


    private fun doEticketing() {
        view.showContinueToETicketing()
    }

    private fun doSurge() {
        val surge = checkin?.screening?.getSurge(checkin?.availability?.startTime, UserPreferences.restrictions.userSegments)
                ?: return
        when (surge.level) {
            SurgeType.NO_SURGE -> {
                when (showProofOfPurchase) {
                    true -> view.showCheckinWithProof()
                    false -> view.showCheckin()
                }
                view.showCheckin()
            }
            SurgeType.WILL_SURGE -> {
                view.showCheckin()
                view.showWillSurge(surge)
            }
            else -> view.showSurge(surge)
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
        when (it.currentlyPeaking) {
            true -> showSurgeModal(it.peakAmount)
            false -> createReservation()
        }
    }

    private fun showSurgeModal(peakAmount: Int) {
        view.showSurgeModal(peakAmount.centsAsDollars)
    }

    private fun createReservation() {
        val checkin = checkin ?: return
        GoWatchItSingleton.getInstance().checkInEvent(checkin.theater, checkin.screening, checkin.availability.startTime
                ?: "", "ticket_purchase", checkin.theater.id.toString(), "")
        val perf = checkin.availability.providerInfo ?: return
        val loc = locationManager.lastLocation() ?: return view.showNeedLocation()
        reservDis?.dispose()
        reservDis = api
                .reserve(checkin, TicketInfoRequest(
                        performanceInfo = perf,
                        latitude = loc.lat,
                        longitude = loc.lon
                )).subscribe({
                    view.navigateTo(checkin, it)
                }, {
                    val apiError = it as? ApiError ?: return@subscribe
                    view.showError(apiError)
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

}
