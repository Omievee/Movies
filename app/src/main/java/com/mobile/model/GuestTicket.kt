package com.mobile.model

import android.os.Parcelable
import com.mobile.utils.text.toCurrency
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuestTicket(
        val ticketType: GuestTicketType? = null,
        val price: Int = 0,
        val seatPosition: SeatPosition? = null,
        val email: String? = null
) : Parcelable {
    val cost: String
        get() {
            return price.div(100.0).toCurrency()
        }

    val costAsDollars: Double
        get() {
            return price.div(100.0)
        }
}