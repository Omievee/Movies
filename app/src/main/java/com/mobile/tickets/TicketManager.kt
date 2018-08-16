package com.mobile.tickets

import com.mobile.model.ProviderInfo
import com.mobile.network.RestrictionsCheckResponse
import com.mobile.requests.TicketInfoRequest
import com.mobile.reservation.Checkin
import com.mobile.responses.ReservationResponse
import io.reactivex.Single

interface TicketManager {
    fun reserve(checkin:Checkin, ticketRequest:TicketInfoRequest): Single<ReservationResponse>
    fun restrictionCheck(perf: ProviderInfo): Single<RestrictionsCheckResponse>
}