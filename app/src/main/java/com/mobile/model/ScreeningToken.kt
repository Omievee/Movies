package com.mobile.model

import com.mobile.reservation.Checkin
import com.mobile.responses.ETicketConfirmation
import com.mobile.responses.ReservationResponse
import java.text.SimpleDateFormat
import java.util.*

class ScreeningToken(val checkIn:Checkin = Checkin(screening = Screening(), theater = Theater(), availability = Availability()),
                     val reservation: ReservationResponse,
                     val confirmationCode:ETicketConfirmation?=null,
                     val seatSelected:List<SeatSelected>? = null
                     ){
    fun getTimeAsDate(): Date? {
        try {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).parse(checkIn.availability.startTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}