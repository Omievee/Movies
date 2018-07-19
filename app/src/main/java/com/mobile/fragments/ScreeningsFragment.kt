package com.mobile.fragments

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.adapters.MissingCheckinListener
import com.mobile.adapters.ScreeningsAdapter
import com.mobile.adapters.ScreeningsAdapter.Companion.createData
import com.mobile.analytics.AnalyticsManager
import com.mobile.history.HistoryManager
import com.mobile.history.model.ReservationHistory
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.location.LocationManager
import com.mobile.location.UserLocation
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.model.Movie

import com.mobile.network.Api
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.reservation.Checkin
import com.mobile.responses.ScreeningsResponseV2
import com.mobile.screening.MoviePosterClickListener
import com.mobile.screening.NoMoreScreenings
import com.mobile.utils.isComingSoon
import com.mobile.utils.showMovieBottomSheetIfNecessary
import com.mobile.utils.showTheaterBottomSheetIfNecessary
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_screenings.*
import javax.inject.Inject

class ScreeningsFragment : MPFragment(), ShowtimeClickListener, MissingCheckinListener {

    var adapter: ScreeningsAdapter = ScreeningsAdapter(this, this)

    var response: android.util.Pair<List<ReservationHistory>, ScreeningsResponseV2>? = null

    var selected: android.util.Pair<Screening, String?>? = null

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var historyManager: HistoryManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var api: Api

    var disposable: Disposable? = null

    var screeningData: ScreeningsData? = null

    val synopsislistener = object : MoviePosterClickListener {
        override fun onMoviePosterClick(movie: Movie) {
            showFragment(SynopsisFragment.newInstance(movie))
        }
    }

    override fun onClick(screening: Screening, showTime: String) {
        onShowtimeClick(null, screening, showTime)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onShowtimeClick(theater: Theater?, screening: Screening, showtime: String) {
        if (selected?.first == screening && selected?.second == showtime) {
            selected = null
        } else {
            selected = android.util.Pair(screening, showtime)
        }

        when (selected) {
            null -> removeFragment(R.id.checkinFragment)
            else -> {
                val availability = screening.getAvailability(showtime) ?: return
                val mytheater = this.screeningData?.theater ?: theater ?: return
                analyticsManager.onShowtimeClicked(mytheater, screening, availability)
                showFragment(R.id.checkinFragment, com.mobile.reservation.newInstance(Checkin(
                        screening = screening,
                        theater = mytheater,
                        availability = availability)))
            }
        }
        updateAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screeningData = arguments?.getParcelable("data")
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        val dimens = resources.getDimension(R.dimen.bottom_navigation_height)
        recyclerView.addItemDecoration(SpaceDecorator(lastBottom = dimens.toInt()))
        swipeRefresh.setOnRefreshListener {
            fetchTheatersIfNecessary(necessary = true)
        }
        val movie = screeningData?.movie
        movieHeader.visibility = when (movie) {
            null -> View.GONE
            else -> {
                val listener = when(movie.isComingSoon) {
                    true-> null
                    false->synopsislistener
                }
                movieHeader.bind(movie = movie, synopsisListener = listener)
                View.VISIBLE
            }
        }
        theaterHeader.visibility = when (screeningData?.movie) {
            null -> {
                screeningData?.theater?.let {
                    theaterHeader.bind(it)
                }
                View.VISIBLE
            }
            else -> View.GONE
        }
        showTheaterBottomSheetIfNecessary(screeningData?.theater)
        showMovieBottomSheetIfNecessary(screeningData?.movie, movieHeader)
    }

    override fun onResume() {
        super.onResume()
        fetchTheatersIfNecessary()
    }

    private fun fetchTheatersIfNecessary(necessary: Boolean = false) {
        disposable?.dispose()
        when (response) {
            null -> {

            }
            else -> when (necessary) {
                false -> return
            }
        }
        swipeRefresh.isRefreshing = true
        val location = locationManager.lastLocation() ?: return showLocationError()
        disposable = observ(location)
                .doAfterTerminate { swipeRefresh.isRefreshing = false }
                .subscribe({
                    response = it
                    updateAdapter()
                }, {
                    when (it) {
                        is ApiError -> showError(it)
                        is NoScreeningsException -> {
                        }
                        else -> showError()

                    }
                })
    }

    private fun showLocationError() {
    }

    private fun observ(location: UserLocation): Observable<Pair<List<ReservationHistory>, ScreeningsResponseV2>> {
        when (screeningData?.movie?.isComingSoon) {
            true -> return Observable.error(NoScreeningsException())
        }
        val theaterId = screeningData?.theater?.tribuneTheaterId
        val screeningsObserv: Observable<ScreeningsResponseV2> = when (theaterId) {
            null -> api.getScreeningsForMovieRx(location.lat, location.lon, screeningData?.movie?.id
                    ?: 0).toObservable()
            else -> api.getScreeningsForTheaterV2(theaterId).toObservable()
        }
        return Observable.zip(
                historyManager.getHistory(), screeningsObserv,
                BiFunction { t1, t2 -> Pair(t1, t2) }
        )
    }

    private fun showError(apiError: ApiError) {
        errorView.show(apiError)
    }

    private fun showError() {
        errorView.show()
    }

    private fun updateAdapter() {
        recyclerView.visibility = View.VISIBLE
        val response = response ?: return
        errorView.visibility = View.GONE
        adapter.data = createData(data =
        adapter.data,
                response = response,
                location = locationManager.lastLocation()?.toLocation(),
                selected = selected,
                userSegments = UserPreferences.restrictions.userSegments
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_screenings, container, false)
    }

    companion object {
        fun newInstance(data: ScreeningsData): ScreeningsFragment {
            return ScreeningsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("data", data)
                }
            }
        }
    }
}

fun UserLocation.toLocation(): Location {
    return Location("").apply {
        latitude = lat
        longitude = lon
    }
}

@Parcelize
class ScreeningsData(val theater: Theater? = null, val movie: Movie? = null) : Parcelable