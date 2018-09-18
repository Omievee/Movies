package com.mobile.home

import com.mobile.history.model.ReservationHistory
import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.reservation.CurrentReservationV2

interface HomeActivityView {
    fun logout()
    fun showTicketVerification(it: PopInfo)
    fun showAlert(it: Alert): Any
    fun showForceLogout(it: LogoutInfo)
    fun showConfirmationScreen(it: CurrentReservationV2)
    fun showActivatedCardScreen()
    fun showPeakPassBadge()
    fun showHistoryRateScreen(reservationHistory: ReservationHistory)
    fun hidePeakPassBadge()
    fun showOverSoftCap()
    fun updateBottomNavForTheaters()
    fun updateBottomNavForMovies()
}
