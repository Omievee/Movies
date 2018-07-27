package com.mobile.gowatchit

import com.mobile.reservation.Checkin

class CheckinFailedEvent(checkIn: Checkin) : CheckinEvent(checkIn) {

    init {
        event = "ticket_purchase_attempt"
    }

}
