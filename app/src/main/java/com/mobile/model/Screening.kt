package com.mobile.model

import android.os.Parcelable
import com.mobile.network.SurgeResponse
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
                     val surge: Map<String, Surge> = emptyMap(),
                     val popRequired: Boolean = false,
                     val imageUrl: String? = null,
                     val theaterAddress: String? = null,
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

    fun getSurge(time: String?, userSegments: List<Int>): Surge {
        val surge = surge[time] ?: return Surge.NONE
        if (surge.level == SurgeType.NO_SURGE) {
            return surge
        }
        val independentSurge = when {
            surge.independentUserSegments.isEmpty() -> false
            surge.independentUserSegments.intersect(userSegments).isNotEmpty() -> true
            else -> false
        }
        val dependentSurge = when {
            !surge.screeningSurging -> false
            surge.dependentUserSegments.isEmpty() ||
                    surge.dependentUserSegments.intersect(userSegments).isNotEmpty() -> true
            else -> false
        }
        return when (independentSurge || dependentSurge) {
            true -> surge
            false -> Surge.NONE
        }
    }

    fun getTicketType(): TicketType? {
        return availabilities
                .firstOrNull {
                    when (it.ticketType) {
                        null -> false
                        else -> true
                    }
                }?.ticketType
    }

    fun updateSurge(availability: Availability?, surgeResponse: SurgeResponse) {
        val avail = availability?:return
        val surge = surge[avail.startTime]?:return
        when(surge!== Surge.NONE) {
            true-> {
                when(surgeResponse.currentlyPeaking) {
                    true-> {
                        surge.level = SurgeType.SURGING
                        surge.amount = surgeResponse.peakAmount
                    }
                }
            }
        }
    }

    companion object {
        fun from(r: CurrentReservationV2): Screening {
            return Screening(
                    moviepassId = r.reservation?.moviepassId,
                    theaterName = r.theater,
                    title = r.title,
                    tribuneTheaterId = r.reservation?.tribuneTheaterId ?: 0
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

