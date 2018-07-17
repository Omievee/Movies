package com.mobile.tickets

import com.mobile.UserPreferences
import com.mobile.model.ProviderInfo
import com.mobile.model.ScreeningToken
import com.mobile.network.Api
import com.mobile.network.SurgeResponse
import com.mobile.requests.TicketInfoRequest
import com.mobile.reservation.Checkin
import com.mobile.responses.ReservationResponse
import io.reactivex.Single

class TicketManagerImpl(val api: Api) : TicketManager {

    override fun peakCheck(perf: ProviderInfo): Single<SurgeResponse> {
        return api.surgeCheck(perf)
    }

    override fun reserve(checkin: Checkin, ticketRequest: TicketInfoRequest): Single<ReservationResponse> {
        return api.reserve(ticketRequest).doOnSuccess {
            UserPreferences.saveReservation(
                    ScreeningToken(
                            checkIn = checkin,
                            reservation = it
                    ))
        }
    }

}