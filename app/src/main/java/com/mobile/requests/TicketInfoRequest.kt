package com.mobile.requests

import com.mobile.model.GuestTicket
import com.mobile.model.PerformanceInfoV2

data class TicketInfoRequest(val performanceInfo: PerformanceInfoV2,
                             val seatPosition: SelectedSeat? = null,
                             val guestTickets: List<GuestTicket>? = null,
                             val latitude: Double,
                             val longitude: Double
)