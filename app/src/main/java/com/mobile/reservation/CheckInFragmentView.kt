package com.mobile.reservation

import com.mobile.ApiError
import com.mobile.model.Surge
import com.mobile.responses.ReservationResponse

interface CheckInFragmentView {
    fun showContinueToETicketing()
    fun showCheckin()
    fun showWillSurge(surge: Surge)
    fun showSurge(surge: Surge)
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

}
