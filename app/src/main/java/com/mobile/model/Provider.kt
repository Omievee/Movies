package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Provider(var providerName: String? = null,
                    var theater: Int = 0,
                    var ticketType: String? = null,
                    var performanceInfo: Map<String, PerformanceInfo?>? = emptyMap()) : Parcelable {

    fun getPerformanceInfo(key: String): PerformanceInfo? {
        return performanceInfo?.get(key)
    }

    fun ticketTypeIsStandard(): Boolean {
        return ticketType?.matches("STANDARD".toRegex()) ?: false
    }

    fun ticketTypeIsETicket(): Boolean {
        return ticketType?.matches("E_TICKET".toRegex()) ?: false
    }

    fun ticketTypeIsSelectSeating(): Boolean {
        return ticketType?.matches("SELECT_SEATING".toRegex()) ?: false
    }
}
