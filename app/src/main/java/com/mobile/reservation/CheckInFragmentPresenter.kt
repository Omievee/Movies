package com.mobile.reservation

import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.location.LocationManager
import com.mobile.model.CapType
import com.mobile.model.CappedPlan
import com.mobile.model.SurgeType
import com.mobile.model.TicketType
import com.mobile.network.RestrictionsCheckResponse
import com.mobile.requests.TicketInfoRequest
import com.mobile.responses.SubscriptionStatus
import com.mobile.tickets.TicketManager
import io.reactivex.disposables.Disposable

class CheckInFragmentPresenter(val view: CheckInFragmentView, val api: TicketManager, val locationManager: LocationManager, val analyticsManager: AnalyticsManager) {

    var checkin: Checkin? = null
    var restrictionsCheckDisposable: Disposable? = null
    var reservDis: Disposable? = null

    fun onCreate(checkin: Checkin?) {
        this.checkin = checkin
        checkin?.clearPasses()
        display()
    }

    private fun display() {
        val check: Checkin = checkin ?: return
        doRestrictionsCheck()
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
            return checkin?.availability?.isETicket() == false && (checkin?.screening?.popRequired == true || UserPreferences.restrictions.proofOfPurchaseRequired)
        }

    private fun doRestrictionsCheck() {
        val checkin = checkin ?: return
        val surge = checkin.screening.getSurge(checkin.availability.startTime, UserPreferences.restrictions.userSegments)
        val peak = UserPreferences.restrictions.peakPassInfo
        when (surge.level) {
            SurgeType.NO_SURGE -> {
                val cappedPlan = UserPreferences.restrictions.cappedPlan
                val isMovieWhiteListed = UserPreferences.restrictions.capWhitelistedMovieIds.contains(checkin.screening.moviepassId)
                when(isMovieWhiteListed){
                    true -> {
                        view.showCheckin(checkin)
                        if (showProofOfPurchase) {
                            view.showCheckinWithProof()
                        }
                    }
                    false -> {
                        when (cappedPlan?.isOverSoftCap) {
                            true -> view.showSoftCapMessage(checkin = checkin, cappedPlan = cappedPlan)
                            else -> {
                                view.showCheckin(checkin)
                                if (showProofOfPurchase) {
                                    view.showCheckinWithProof()
                                }
                            }
                        }
                    }
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

    val skipRestrictionsCheck: Boolean
        get() {
            val checkin = checkin ?: return false
            if (checkin.availability.isETicket()) {
                return false
            }
            val surge = checkin.screening.getSurge(checkin.availability.startTime, UserPreferences.restrictions.userSegments)
//            val cap = UserPreferences.restrictions.cappedPlan
//            val isMovieWhiteListed = UserPreferences.restrictions.capWhitelistedMovieIds.contains(checkin.screening.moviepassId)
//            if (cap?.isOverSoftCap == true && !isMovieWhiteListed) {
//                return true
//            }
//            if (cap?.isOverHardCap == false) {
//                return false
//            }
            if (surge.level == SurgeType.SURGING) {
                return true
            }
            return false
        }

    fun onContinueClicked() {
        restrictionsCheckDisposable?.dispose()
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
        if (skipRestrictionsCheck) {
            return skipRestrictionsCheck()
        }
        view.showProgress()
        restrictionsCheckDisposable = api.restrictionCheck(
                payload
        ).doAfterTerminate {
            view.hideProgress()
        }.subscribe({
            onRestrictionsCheckResponse(it)
        }, {
            when (it) {
                is ApiError -> view.showError(it)
                else -> view.showGenericError()
            }
        })
    }

    private fun skipRestrictionsCheck() {
        val checkin = checkin ?: return
        val surge = checkin.screening.getSurge(checkin.availability.startTime, UserPreferences.restrictions.userSegments)
//        val cap = UserPreferences.restrictions.cappedPlan
//        if (cap?.isOverSoftCap == true) {
//            checkin.softCap = true
//            return view.navigateToSoftCapCheckout(checkin)
//        }
        if (surge.level == SurgeType.SURGING) {
            val pinfo = UserPreferences.restrictions.peakPassInfo
            if (pinfo.currentPeakPass != null) {
                checkin.peakPass = pinfo.currentPeakPass
                return view.showApplyPeakPass(checkin, pinfo, pinfo.currentPeakPass)
            } else {
                view.navigateToSurchargeConfirm(checkin)
            }
        }
    }

    private fun onRestrictionsCheckResponse(it: RestrictionsCheckResponse) {
        val checkin = checkin ?: return
        when {
            it.data.overSoftCap && checkin.availability.isETicket() && UserPreferences.restrictions.cappedPlan?.isOverSoftCap==true -> {
                UserPreferences.restrictions.apply {
                    val old = cappedPlan
                    cappedPlan = CappedPlan(
                            type = when (it.data.overSoftCap) {
                                true -> CapType.SOFT
                                false -> CapType.HARD
                            },
                            remaining = 0,
                            used = old?.used ?: 3,
                            discount = it.data.attributes?.discount ?: 0
                    )
                }
                checkin.softCap = true
                view.navigateToSoftCapCheckout(checkin)
            }
            (it.data.overSoftCap || it.data.overHardCap) -> {
                UserPreferences.restrictions.apply {
                    val old = cappedPlan
                    cappedPlan = CappedPlan(
                            type = when (it.data.overSoftCap) {
                                true -> CapType.SOFT
                                false -> CapType.HARD
                            },
                            remaining = 0,
                            used = old?.used ?: 3,
                            discount = it.data.attributes?.discount ?: 0
                    )
                }
                view.showOverCap(UserPreferences.restrictions.cappedPlan, it)
                doRestrictionsCheck()
            }
            it.data.attributes?.currentlyPeaking == true -> {
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
                doRestrictionsCheck()
            }
            true -> createReservation()
        }
    }

    private fun showSurgeModal(surge: RestrictionsCheckResponse) {
        view.showSurgeModal(surge)
    }

    private fun createReservation() {
        val checkin = checkin ?: return
        val perf = checkin.availability.providerInfo ?: return
        val loc = locationManager.lastLocation() ?: return view.showNeedLocation()
        if (checkin.availability.isETicket()) {
            return view.navigateToCreateReservation(checkin)
        }
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
                    when (it) {
                        is ApiError -> {
                            view.showError(it)
                        }
                        else -> {
                            view.showGenericError()
                        }
                    }
                    analyticsManager.onCheckinFailed(checkin)
                })
    }

    fun onDestroy() {
        restrictionsCheckDisposable?.dispose()
        reservDis?.dispose()
    }

    fun onContinueDialogClicked(restrictions: RestrictionsCheckResponse) {
        val checkin = checkin ?: return
        when {
            restrictions.data.overSoftCap -> {
                checkin.softCap = true
                view.navigateToSoftCapCheckout(checkin)
            }
            restrictions.data.attributes?.currentlyPeaking == true -> view.navigateToSurchargeConfirm(checkin)
            restrictions.data.overHardCap -> {
            }
        }
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
