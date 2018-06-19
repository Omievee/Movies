package com.mobile.home

import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.model.ScreeningToken
import com.mobile.reservation.CurrentReservationV2
import com.mobile.responses.MicroServiceRestrictionsResponse

interface HomeActivityView {
    fun logout()
    fun showTicketVerification(it: PopInfo)
    fun showAlert(it: Alert): Any
    fun showForceLogout(it: LogoutInfo)
    fun showConfirmationScreen(it: CurrentReservationV2)
}
