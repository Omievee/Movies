package com.mobile.fragments

import android.util.Pair
import com.mobile.ApiError
import com.mobile.history.model.ReservationHistory
import com.mobile.location.UserLocation
import com.mobile.model.Movie
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.reservation.Checkin
import com.mobile.responses.ScreeningsResponseV2

interface ScreeningsFragmentView : LocationRequiredView {
    fun removeCheckinFragment()
    fun setMovieHeader(movie: Movie, synopssListener:Boolean)
    fun setTheaterHeader(theater: Theater)
    fun showCheckinFragment(checkin: Checkin)
    fun updateAdapter(response: Pair<List<ReservationHistory>, ScreeningsResponseV2>, location: UserLocation?, selected: Pair<Screening, String?>?, segments: List<Int>)
    fun showRefreshing()
    fun showLocationError()
    fun notRefreshing()
    fun showError(it: ApiError)
    fun showError()
    fun showTheaterBottomSheetNecessary(theater: Theater?)
    fun showSynopsis(movie:Movie)
    fun hideSynopsis()
    fun surgeInterstitialFlow(screening: ScreeningsResponseV2,lastCode:Int=0)
    fun setRecyclerSpacing(topSpacing:Boolean)
}
