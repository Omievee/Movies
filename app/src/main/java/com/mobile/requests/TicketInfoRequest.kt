package com.mobile.requests

import com.mobile.model.GuestTicket
import com.mobile.model.ProviderInfo

data class TicketInfoRequest(val performanceInfo: ProviderInfo,
                             val seatPosition: SelectedSeat? = null,
                             val guestTickets: List<GuestTicket>? = null,
                             val latitude: Double,
                             val longitude: Double
)