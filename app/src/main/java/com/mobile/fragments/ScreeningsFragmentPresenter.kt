package com.mobile.fragments

import android.util.Pair
import android.view.View
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.history.HistoryManager
import com.mobile.history.model.ReservationHistory
import com.mobile.location.LocationManager
import com.mobile.location.UserLocation
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.network.Api
import com.mobile.reservation.Checkin
import com.mobile.responses.ScreeningsResponseV2
import com.mobile.utils.isComingSoon
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction

class ScreeningsFragmentPresenter(override val view:ScreeningsFragmentView, val screeningData: ScreeningsData, val api:Api, locationManager:LocationManager, val historyManager: HistoryManager, val analyticsManager: AnalyticsManager) : LocationRequiredPresenter(view, locationManager) {

    var response: android.util.Pair<List<ReservationHistory>, ScreeningsResponseV2>? = null

    var selected: android.util.Pair<Screening, String?>? = null

    var screeningsSub: Disposable? = null

    override fun onLocation(userLocation: UserLocation) {
        fetchTheatersIfNecessary(true)
    }

    fun onShowtimeClick(theater: Theater?, screening: Screening, showtime: String) {
        if (selected?.first == screening && selected?.second == showtime) {
            selected = null
        } else {
            selected = android.util.Pair(screening, showtime)
        }

        when (selected) {
            null -> view.removeCheckinFragment()
            else -> {
                val availability = screening.getAvailability(showtime) ?: return
                val mytheater = this.screeningData?.theater ?: theater ?: return
                analyticsManager.onShowtimeClicked(mytheater, screening, availability)
                view.showCheckinFragment(Checkin(screening,mytheater,availability))
            }
        }
        val response = response?:return
        view.updateAdapter(response, location, selected, UserPreferences.restrictions.userSegments)
    }

    fun onViewCreated() {
        view.hideSynopsis()
        val movie = screeningData.movie
        when (movie) {
            null -> {
            }
            else -> {
                when (movie.isComingSoon) {
                    true -> {
                        view.setMovieHeader(movie, false)
                        view.showSynopsis(movie)
                    }
                    false -> view.setMovieHeader(movie, true)
                }
            }
        }
        val theater = screeningData.theater
        when(theater) {
            null-> {}
            else-> {
                view.setTheaterHeader(theater)
            }
        }
        view.showTheaterBottomSheetNecessary(theater)
    }

    private fun observ(location: UserLocation): Observable<Pair<List<ReservationHistory>, ScreeningsResponseV2>> {
        when (screeningData.movie?.isComingSoon) {
            true -> return Observable.error(NoScreeningsException())
        }
        val theaterId = screeningData.theater?.tribuneTheaterId
        val screeningsObserv: Observable<ScreeningsResponseV2> = when (theaterId) {
            null -> api.getScreeningsForMovieRx(location.lat, location.lon, screeningData.movie?.id
                    ?: 0).toObservable()
            else -> api.getScreeningsForTheaterV2(theaterId).toObservable()
        }
        return Observable.zip(
                historyManager.getHistory(), screeningsObserv,
                BiFunction { t1, t2 -> Pair(t1, t2) }
        )
    }

    private fun fetchTheatersIfNecessary(necessary: Boolean = false) {
        screeningsSub?.dispose()
        when (response) {
            null -> {

            }
            else -> when (necessary) {
                false -> return
            }
        }
        view.showRefreshing()
        val location = locationManager.lastLocation() ?: return view.showLocationError()
        screeningsSub = observ(location)
                .doAfterTerminate { view.notRefreshing() }
                .subscribe({
                    response = it
                    view.updateAdapter(it, location, selected, UserPreferences.restrictions.userSegments)
                }, {
                    when (it) {
                        is ApiError -> view.showError(it)
                        is NoScreeningsException -> {
                        }
                        else -> view.showError()

                    }
                })
    }

    fun onRefresh() {
        if(location==null) {
            return onPrimary()
        }
        fetchTheatersIfNecessary(necessary = true)
    }

    fun onResume() {
        if(location==null) {
            return onPrimary()
        } else {
            fetchTheatersIfNecessary(necessary = false)
        }
    }
}