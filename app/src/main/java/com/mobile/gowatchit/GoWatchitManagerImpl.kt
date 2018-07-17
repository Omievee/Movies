package com.mobile.gowatchit

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.application.Application
import com.mobile.location.LocationManager
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.reservation.Checkin
import com.mobile.reservation.CurrentReservationV2
import com.mobile.rx.Schedulers
import io.reactivex.Single

class GoWatchitManagerImpl(val goWatchItApi: GoWatchItApi, val gson: Gson, val locationManager: LocationManager, val application: Application) : GoWatchItManager {

    override fun onCheckInFailed(checkIn: Checkin) {
        send(CheckinFailedEvent(checkIn))
    }

    override fun onCheckInSuccessful(checkIn: Checkin) {
        send(CheckinEvent(checkIn))
    }

    override fun onTicketPurchasedConfirmation(checkIn: Checkin, reservationV2: CurrentReservationV2) {
        send(TicketPurchase(checkIn))
    }

    override fun onTheaterListOpened() {
        send(OpenedTheaterList())
    }

    override fun onTheaterMapOpened() {
        send(OpenedTheaterMap())
    }

    override fun onTheaterOpened(theater: Theater) {
        send(OpenedTheater(theater))
    }

    override fun onMovieSearched(query: String) {
        send(MovieSearch(query))
    }

    private val campaign: String?
        get() {
            val campaignStr = application
                    .activityStack
                    .map { activity->
                        activity.intent.data.mapQueryNames()
                    }
                    .flatMap {
                        it.entries
                    }.map {
                        when(it.key) {
                            "campaign" -> it.value
                            "utc_campaign" -> it.value
                            "c" -> it.value
                            else-> null
                        }
                    }.filterNotNull()
                    .firstOrNull()
            return campaignStr
        }

    override fun userOpenedApp() {
        send(AppOpened())
    }

    override fun userClickedOnShowtime(theater: Theater, screening: Screening, availability: Availability) {
        send(ClickedShowtime(theater = theater, screening = screening, availability = availability))
    }

    override fun onTheaterSearch(query: String) {
        send(TheaterSearch(query))
    }

    fun send(obj: Any) {
        val single: Single<Any> = Single.create {
            val mutablemap = mutableMapOf<String, String>()
            mutablemap.putAll(toMap(obj))
            mutablemap.putAll(toMap(GoWatchit(campaign = campaign)))
            mutablemap.putAll(toMap(locationManager.lastLocation()))
            goWatchItApi.send(mutablemap).compose(Schedulers.singleBackground()).subscribe { _, _ -> }

        }
        single.compose(Schedulers.singleBackground())
                .subscribe { _, _ -> }
    }

    fun toMap(obj: Any?): Map<String, String> {
        obj ?: return emptyMap()
        val str = gson.toJson(obj)
        val map: Map<String, String> = gson.fromJson(str, object : TypeToken<Map<String, String>>() {}.type)
        return map
    }

}

fun Uri.mapQueryNames(): Map<String, String> {
    return queryParameterNames.associateTo(mutableMapOf()) { key -> Pair(key, getQueryParameter(key)) }
}