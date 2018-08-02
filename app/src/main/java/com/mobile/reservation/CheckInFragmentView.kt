package com.mobile.reservation

import com.mobile.ApiError
import com.mobile.model.Surge
import com.mobile.network.SurgeResponse
import com.mobile.responses.PeakPass
import com.mobile.responses.PeakPassInfo
import com.mobile.responses.ReservationResponse

interface CheckInFragmentView {
    fun showContinueToETicketing()
    fun showCheckin()
    fun showWillSurge(surge: Surge, peakPassInfo: PeakPassInfo, peakPass: PeakPass?)
    fun showSurge(surge: Surge, peakPassInfo: PeakPassInfo, peakPass: PeakPass?)
    fun showProgress()
    fun hideProgress()
    fun showNeedLocation()
    fun showError(apiError: ApiError)
    fun showGenericError()
    fun navigateTo(checkin: Checkin, reservation: ReservationResponse)
    fun showSurgeModal(peakAmount: String)
    fun navigateToCreateReservation(checkin: Checkin)
    fun navigateToSurchargeConfirm(checkin: Checkin)
    fun showActivateCard(checkin:Checkin)
    fun showCheckinWithProof()
    fun showApplyPeakPass(checkin: Checkin, peakPasses: PeakPassInfo, currentPeakPass: PeakPass?)
    fun showNowPeakingApplyPeakPass(it: SurgeResponse, peak: PeakPassInfo, peakPass: PeakPass)
    fun showNowPeakingNoPeakPass(checkin: Checkin, surge: Surge)
    fun showPeakPassSheet(checkin: Checkin, peak: PeakPassInfo, peakPass: PeakPass?)

}
