package com.mobile.model

import android.os.Parcelable
import com.mobile.UserPreferences
import com.mobile.network.RestrictionsCheckResponse
import com.mobile.reservation.CurrentReservationV2
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Screening(var moviepassId: Int? = null,
                     val title: String? = null,
                     val approved: Boolean = false,
                     val tribuneTheaterId: Int = 0,
                     val landscapeImageUrl: String? = null,
                     val qualifiersApproved: Boolean = false,
                     val availabilities: List<Availability> = emptyList(),
                     val surge: MutableMap<String, Surge> = mutableMapOf(),
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

    fun isSurging(userSegments: List<Int>):Boolean {
        return surge.values.any { surge->
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
            val surging = independentSurge||dependentSurge
            surging
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

    fun updateSurge(availability: Availability?, surgeResponse: RestrictionsCheckResponse) {
        val avail = availability ?: return
        var surging = surge[avail.startTime]
        if (surging == null) {
            surging = Surge(
                    independentUserSegments = UserPreferences.restrictions.userSegments)
            surge[avail.startTime ?: ""] = surging
        }
        surging.screeningSurging = surgeResponse.data.attributes?.currentlyPeaking==true
        surging.level = SurgeType.SURGING
        surging.amount = surgeResponse.data.attributes?.peakAmount?:0
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
