package com.mobile.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mobile.activities.TicketType
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class ProviderInfo(
        val normalizedMovieId: Int? = 0,
        val externalMovieId: String? = null,
        val format: Format? = null,
        val performanceId: String? = null,
        val dateTime: ParcelableDate? = null,
        val seatPosition: SeatPosition? = null,
        val guestsAllowed: Int? = null,
        val guestTickets: List<GuestTicket>? = null,
        val tribuneTheaterId:Int = 0
) : Parcelable

enum class Format {
    @SerializedName("2D", alternate = ["2d"])
    `2D`,
    @SerializedName("3D", alternate = ["3d"])
    `3D`
}