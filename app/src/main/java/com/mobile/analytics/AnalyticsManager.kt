package com.mobile.analytics

import com.mobile.model.*
import com.mobile.reservation.Checkin
import com.mobile.responses.ReservationResponse

interface AnalyticsManager {

    fun onUserLoggedIn(user:User)
    fun onUserLoggedOut(user:User?)
    fun onAppOpened()
    fun onShowtimeClicked(mytheater: Theater, screening: Screening, availability: Availability)
    fun onTheaterOpened(theater:Theater)
    fun onMovieImpression(movie:Movie)
    fun onTheaterMapOpened()
    fun onTheaterListOpened()
    fun onTheaterSearch(query:String)
    fun onMovieSearch(query:String)
    fun onCheckinAttempt(checkIn: Checkin)
    fun onCheckinFailed(checkIn: Checkin)
    fun onCheckinSuccessful(checkIn: Checkin, reservationResponse: ReservationResponse)
    fun onTheaterTabOpened()
    fun onBrazeDataSetUp(user: User)
    fun onUserChangedNotificationsSubscriptions(permissionToggle: Boolean)
}