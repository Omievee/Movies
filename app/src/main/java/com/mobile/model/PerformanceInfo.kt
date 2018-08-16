package com.mobile.model

import android.os.Parcelable
import com.mobile.reservation.TicketType
import kotlinx.android.parcel.Parcelize

@Parcelize
class PerformanceInfo(
        var externalMovieId: String? = null,
        var format: String? = null,
        var tribuneTheaterId: Int = 0,
        var screeningId: Int = 0,
        var dateTime: String? = null,
        var performanceNumber: Int = 0,
        var sku: String? = null,
        var price: Double? = null,
        var auditorium: String? = null,
        var performanceId: String? = null,
        var sessionId: String? = null,
        var normalizedMovieId: Int = 0,
        var ticketType: TicketType? = null,
        var cinemaChainId: String? = null,
        var showtimeId: String? = null
) : Parcelable
