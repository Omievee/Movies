package com.mobile.analytics

import com.appboy.Appboy
import com.appboy.enums.NotificationSubscriptionType
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.answers.LoginEvent
import com.crashlytics.android.answers.PurchaseEvent
import com.mobile.UserPreferences
import com.mobile.application.Application
import com.mobile.gowatchit.GoWatchItManager
import com.mobile.model.*
import com.mobile.reservation.Checkin
import com.mobile.responses.ReservationResponse
import java.lang.String.valueOf
import java.math.BigDecimal
import java.util.*

class AnalyticsManagerImpl(val context: Application, val goWatchItManager: GoWatchItManager) : AnalyticsManager {


    var appBoy = Appboy.getInstance(context)

    override fun onCheckinAttempt(checkIn: Checkin) {
        UserPreferences.setLastCheckInAttempt(checkIn)
    }

    override fun onCheckinFailed(checkIn: Checkin) {
        goWatchItManager.onCheckInFailed(checkIn)
    }

    override fun onMovieImpression(movie: Movie) {
        goWatchItManager.onMovieImpression(movie)
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

    override fun onCheckinSuccessful(checkIn: Checkin, reservation: ReservationResponse) {
        goWatchItManager.onCheckInSuccessful(checkIn)
        UserPreferences.saveReservation(ScreeningToken(
                checkIn = checkIn,
                reservation = reservation
        ))
        val surge = checkIn.getSurge(UserPreferences.restrictions.userSegments)
        when {
            checkIn.peakPass == null && surge.level == SurgeType.SURGING -> {
                Answers.getInstance().logPurchase(PurchaseEvent().putItemName("peak_pricing")
                        .putCurrency(Currency.getInstance(Locale.US)).putItemPrice(surge.amount.toBigDecimal().divide(BigDecimal(100.0))))
            }
            else -> {

            }
        }
    }

    override fun onMovieSearch(query: String) {
        goWatchItManager.onMovieSearched(query)
        Answers.getInstance().logCustom(CustomEvent("movie_search").putCustomAttribute("query", query))
    }

    override fun onTheaterSearch(query: String) {
        goWatchItManager.onTheaterSearch(query)
        Answers.getInstance().logCustom(CustomEvent("theater_search").putCustomAttribute("query", query))
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
        val campaign = goWatchItManager.campaign
        when (campaign) {
            null -> {
            }
            else -> Answers.getInstance().logCustom(CustomEvent("campaign").putCustomAttribute("key", campaign))
        }

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
        onBrazeDataSetUp(user)
        Answers.getInstance().logLogin(LoginEvent().putSuccess(true))
    }

    override fun onUserLoggedOut(user: User?) {
        appBoy.closeSession(context.currentActivity)
    }


    override fun onBrazeDataSetUp(user: User?) {
        val brazeUser = user ?: return

        appBoy.changeUser(valueOf(brazeUser.id))
        appBoy.currentUser.setFirstName(brazeUser.firstName)
        appBoy.currentUser.setLastName(brazeUser.lastName)
        appBoy.currentUser.setEmail(brazeUser.email)

    }

    override fun onUserChangedNotificationsSubscriptions(permissionToggle: Boolean) {
        when (permissionToggle) {
            true -> appBoy.currentUser.setPushNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN)
            false -> appBoy.currentUser.setPushNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED)
        }


    }
    override fun onTheaterTabOpened() {
        Answers.getInstance().logCustom(CustomEvent("theater_tab_opened"))
    }
}