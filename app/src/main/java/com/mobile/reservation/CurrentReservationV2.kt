package com.mobile.reservation

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mobile.activities.TicketType
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class CurrentReservationV2(
        @SerializedName("e_ticket")
        var ticket: ETicket? = null,
        var landscapeUrl: String? = null,
        var latitude: Double? = null,
        var longitude: Double? = null,
        var reservation: Reservation2? = null,
        var theater: String? = null,
        var title: String? = null,
        var zip: String? = null
) : Parcelable

@Parcelize
data class ETicket(
        @SerializedName("confirmationCodeFormat")
        var confirmationCodeFormat: String? = null,
        @SerializedName("redemption_code", alternate =["redemptionCode"])
        var redemptionCode: String? = null,
        var seat: String? = null
) : Parcelable {

    @IgnoredOnParcel
    var format: TicketFormat? = null
    get() {
        return TicketFormat.values().find {
            it.name == confirmationCodeFormat?.toUpperCase()
        }
    }
}

    @Parcelize
    data class Reservation2(
            var checkinId: String? = null,
            var createdAt: Long? = null,
            var id: Int? = null,
            var kind: String? = null,
            var mappingId: String? = null,
            var moviepassId: Int = 0,
            @SerializedName("showtime") var _showtime: Long? = null,
            var subscriptionId: String? = null,
            var tribuneTheaterId: Int = 0,
            var userId: Int? = null
    ) : Parcelable {
        @IgnoredOnParcel
        @Transient
        var showtime: Date? = null
            get() {
                return _showtime?.let {
                    return Date(it)
                }
            }
    }

    enum class TicketFormat {
        UNKNOWN,
        QRCODE,
        PDF417
    }