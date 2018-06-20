package com.mobile.gowatchit

import com.mobile.reservation.Checkin

open class CheckinEvent(checkin:Checkin) : TicketPurchase(checkin) {

    init {
        event = "ticket_purchase"
    }
}