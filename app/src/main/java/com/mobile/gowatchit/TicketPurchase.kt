package com.mobile.gowatchit

import com.mobile.reservation.Checkin

open class TicketPurchase(checkIn: Checkin): ClickedShowtime(checkIn.theater, checkIn.screening, checkIn.availability) {

    init {
        event = "ticket_purchase_confirmation"
    }


}
