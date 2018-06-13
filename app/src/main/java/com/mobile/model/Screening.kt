package com.mobile.model

import android.os.Parcelable
import com.mobile.reservation.CurrentReservationV2
import kotlinx.android.parcel.Parcelize
import java.util.Date;

@Parcelize
data class Screening(var moviepassId: Int? = null,
                     val title: String? = null,
                     val approved: Boolean = false,
                     val tribuneTheaterId: Int = 0,
                     val landscapeImageUrl: String? = null,
                     val qualifiersApproved: Boolean = false,
                     val availabilities: List<Availability> = emptyList(),
                     val popRequired: Boolean = false,
                     val imageUrl: String? = null,
                     val theaterAddress:String? = null,
                     val theaterName: String? = null,
                     val date: ParcelableDate? = null,
                     val disabledExplanation: String? = null,
                     val runningTime: Int = 0,
                     val rating: String? = null,
                     val maximumGuests: Int = 0,
                     val kind: String? = null
) : Parcelable {


    fun getAvailability(time: String?): Availability? {
        return availabilities
                .first {
                    it.startTime == time
                }
    }

    fun getTicketType(): TicketType? {
        return availabilities
                .firstOrNull {
                    when(it.ticketType) {
                        null->false
                        else->true
                    }
                }?.ticketType
    }

    companion object {
        fun from(r: CurrentReservationV2): Screening {
            return Screening(
                    moviepassId = r.reservation?.moviepassId,
                    theaterName = r.theater,
                    title = r.title,
                    tribuneTheaterId = r.reservation?.tribuneTheaterId?:0
            )
        }
    }
}

fun from(theater: Theater): Theater2 {
    return Theater2(id = theater.id,
            name = theater.name,
            latitude = theater.lat,
            longitude = theater.lon,
            tribuneTheaterId = theater.tribuneTheaterId
    )
}

