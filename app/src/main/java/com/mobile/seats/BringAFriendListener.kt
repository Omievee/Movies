package com.mobile.seats

import com.mobile.model.SeatInfo
import com.mobile.model.SeatPosition
import com.mobile.responses.ReservationResponse
import io.reactivex.Observable

interface BringAFriendListener {
    fun onGuestsContinue(payload: List<TicketPurchaseData>)
    fun payload(): Observable<SelectSeatPayload>
    fun onBackPressed()
    fun onClosePressed()
    fun onSeatSelectionContinue(payload: SelectSeatPayload?)
    fun onEmailContinueClicked(payload: SelectSeatPayload?)
    fun onTicketsPurchased(result: ReservationResponse)
    fun navigateToSeats(unavailableSeats: List<SeatPosition>)
}