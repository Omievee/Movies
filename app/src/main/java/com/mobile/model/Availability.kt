package com.mobile.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Availability(
        val startTime: String? = null,
        val endTime: String? = null,
        val ticketType: TicketType? = null,
        val available: Boolean = false,
        @SerializedName("providerPerformanceInfo")
        val providerInfo: PerformanceInfoV2? = null,
        val guestsTicketTypes: List<GuestTicket>? = null

) : Parcelable {
    fun isETicket(): Boolean {
        return when (ticketType) {
            TicketType.E_TICKET, TicketType.SELECT_SEATING -> true
            else -> false
        }
    }
}