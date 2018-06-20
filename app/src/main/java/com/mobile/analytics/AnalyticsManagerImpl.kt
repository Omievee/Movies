package com.mobile.analytics

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.answers.LoginEvent
import com.mobile.UserPreferences
import com.mobile.gowatchit.GoWatchItManager
import com.mobile.model.*
import com.mobile.reservation.Checkin
import com.mobile.responses.ReservationResponse
import java.lang.String.valueOf

class AnalyticsManagerImpl(val goWatchItManager: GoWatchItManager) : AnalyticsManager {

    override fun onCheckinFailed(checkIn: Checkin) {
        goWatchItManager.onCheckInFailed(checkIn)
        UserPreferences.setLastCheckInAttemptDate()
    }

    override fun onTheaterListOpened() {
        goWatchItManager.onTheaterListOpened()
        Answers
                .getInstance()
                .logCustom(
                        CustomEvent("theater_list_opened"))
    }

    override fun onTheaterMapOpened() {
        goWatchItManager.onTheaterMapOpened()
        Answers
                .getInstance()
                .logCustom(
                        CustomEvent("theater_map_opened")
                )
    }

    override fun onCheckinSuccessful(checkIn: Checkin, reservation:ReservationResponse) {
        goWatchItManager.onCheckInSuccessful(checkIn)
        UserPreferences.saveReservation(ScreeningToken(
                checkIn = checkIn,
                reservation = reservation
        ))
        UserPreferences.setLastCheckInAttemptDate()
    }

    override fun onMovieSearch(query: String) {
        goWatchItManager.onMovieSearched(query)
        Answers.getInstance().logCustom(CustomEvent("movie_search").putCustomAttribute("query", query))
    }

    override fun onTheaterSearch(query: String) {
        goWatchItManager.onTheaterSearch(query)
        Answers.getInstance().logCustom(CustomEvent("theater_search").putCustomAttribute("query",query))
    }

    override fun onTheaterOpened(theater: Theater) {
        goWatchItManager
                .onTheaterOpened(theater)

        Answers
                .getInstance()
                .logCustom(
                        CustomEvent("theater_opened")
                                .putCustomAttribute("theater_name", theater.name)
                )
    }


    override fun onAppOpened() {
        goWatchItManager.userOpenedApp()
    }

    override fun onShowtimeClicked(mytheater: Theater, screening: Screening, availability: Availability) {
        goWatchItManager
                .userClickedOnShowtime(theater = mytheater, screening = screening, availability = availability)

        val surge = screening.getSurge(availability.startTime, UserPreferences.restrictions.userSegments)

        Answers.getInstance().logCustom(
                CustomEvent("showtime_clicked")
                        .putCustomAttribute("movie_title", screening.title)
                        .putCustomAttribute("surge_level", surge.level.level)
                        .putCustomAttribute("surge_amount", surge.amount)
                        .putCustomAttribute("start_time", availability.startTime)
        )
    }

    override fun onUserLoggedIn(user: User) {
        Crashlytics.setUserEmail(user.email)
        Crashlytics.setUserIdentifier(valueOf(user.id))
        Answers.getInstance().logLogin(LoginEvent().putSuccess(true))
    }

    override fun onUserLoggedOut(user: User?) {

    }

}