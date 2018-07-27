package com.mobile.gowatchit

import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.reservation.Checkin
import com.mobile.reservation.CurrentReservationV2

interface GoWatchItManager {

    fun userClickedOnShowtime(theater: Theater, screening: Screening, availability: Availability)
    fun onTheaterMapOpened()
    fun onTheaterListOpened()
    fun userOpenedApp()
    fun onTheaterOpened(theater: Theater)
    fun onMovieSearched(query: String)
    fun onTheaterSearch(query: String)
    fun onTicketPurchasedConfirmation(checkIn: Checkin, reservationV2: CurrentReservationV2)
    fun onCheckInSuccessful(checkIn: Checkin)
    fun onCheckInFailed(checkIn: Checkin)
    val campaign:String?
}