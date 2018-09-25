package com.mobile.reservation

import android.os.Parcelable
import com.mobile.model.*
import com.mobile.model.TicketType
import com.mobile.responses.PeakPass
import com.mobile.utils.calendar
import com.mobile.utils.timeToCalendar
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.Calendar.*

@Parcelize
data class Checkin(
        var screening: Screening,
        var theater: Theater,
        var availability: Availability,
        var peakPass: PeakPass?=null,
        var time:ParcelableDate = ParcelableDate(timeAsString = System.currentTimeMillis().toString()),
        var softCap:Boolean=false
) : Parcelable {

    fun getSurge(segments:List<Int>):Surge {
        return screening.getSurge(availability.startTime, segments)
    }

    fun clearPasses() {
        peakPass = null
        softCap = false
    }

    @IgnoredOnParcel
    val showGuestFlow:Boolean by lazy {
        val tot = screening.maximumGuests>0 && availability.guestsTicketTypes?.isEmpty() == false && availability.ticketType==TicketType.SELECT_SEATING
        tot
    }


    @IgnoredOnParcel
    val showDateTime:Date?
    get() {
        val calendar = screening.date?.calendar?: getInstance()
        val time = availability.startTime?.timeToCalendar?:return null
        calendar.apply {
            set(HOUR_OF_DAY, time.get(HOUR_OF_DAY))
            set(MINUTE, time.get(MINUTE))
        }
        return calendar.time
    }

    @IgnoredOnParcel
    val hasAdultTicketPrice:Boolean
        get() {
            return softCap!=true && availability.guestsTicketTypes?.any { it.ticketType== GuestTicketType.ADULT_COMPANION } == true
        }
}