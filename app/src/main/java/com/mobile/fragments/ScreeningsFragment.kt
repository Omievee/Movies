package com.mobile.fragments

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.adapters.MissingCheckinListener
import com.mobile.adapters.ScreeningsAdapter
import com.mobile.adapters.ScreeningsAdapter.Companion.createData
import com.mobile.history.model.ReservationHistory
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.location.UserLocation
import com.mobile.model.Movie
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.reservation.Checkin
import com.mobile.responses.ScreeningsResponseV2
import com.mobile.screening.MoviePosterClickListener
import com.mobile.screenings.PinnedBottomSheetBehavior
import com.mobile.surge.PeakPricingActivity
import com.mobile.utils.highestElevation
import com.mobile.utils.isComingSoon
import com.mobile.utils.showTheaterBottomSheetIfNecessary
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_screenings.*
import javax.inject.Inject

class ScreeningsFragment : LocationRequiredFragment(), ShowtimeClickListener, MissingCheckinListener, ScreeningsFragmentView {

    var adapter: ScreeningsAdapter = ScreeningsAdapter(this, this)

    @Inject
    lateinit var presenter: ScreeningsFragmentPresenter

    val synopsislistener = object : MoviePosterClickListener {
        override fun onMoviePosterClick(movie: Movie) {
            showSynopsis(movie)
        }
    }

    override fun showSynopsis(movie: Movie) {
        val bottom = BottomSheetBehavior.from(synopsisBottomSheetView) as? PinnedBottomSheetBehavior
                ?: return
        synopsisBottomSheetView.bind(movie)
        bottom.state = BottomSheetBehavior.STATE_COLLAPSED
        synopsisBottomSheetView.elevation = appBarLayout.elevation + appBarLayout.highestElevation
        bottom.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        appBarLayout.setExpanded(true)
                    }
                }
            }

        })
        when (movie.isComingSoon) {
            true -> bottom.locked = true
        }
    }

    override fun surgeInterstitialFlow(second: ScreeningsResponseV2) {
        val context = context?:return
        if(!UserPreferences.shownPeakPricing && second.isSurging(UserPreferences.restrictions.userSegments)) {
            startActivity(PeakPricingActivity.newInstance(context))
        }
    }

    override fun hideSynopsis() {
        val bottom = BottomSheetBehavior.from(synopsisBottomSheetView)
        bottom.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun presenter(): LocationRequiredPresenter {
        return presenter
    }

    override fun onPrimary() {
        presenter.onPrimary()
    }

    override fun showLocationError() {
    }

    override fun showTheaterBottomSheetNecessary(theater: Theater?) {
        showTheaterBottomSheetIfNecessary(theater)
    }

    override fun showProgress() {
        swipeRefresh.isRefreshing = true
    }

    override fun hideProgress() {
        swipeRefresh.isRefreshing = false
    }

    override fun showCheckinFragment(checkin: Checkin) {
        showFragment(R.id.checkinFragment, com.mobile.reservation.newInstance(checkin))
    }

    override fun onClick(screening: Screening, showTime: String) {
        presenter.onShowtimeClick(null, screening, showTime)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onShowtimeClick(theater: Theater?, screening: Screening, showtime: String) {
        presenter.onShowtimeClick(theater, screening, showtime)
    }

    override fun removeCheckinFragment() {
        removeFragment(R.id.checkinFragment)
    }

    override fun setMovieHeader(movie: Movie, synopsisListener: Boolean) {
        movieHeader.visibility = View.VISIBLE
        when (synopsisListener) {
            true -> movieHeader.bind(movie, this.synopsislistener)
            false -> movieHeader.bind(movie, null)
        }
    }

    override fun setTheaterHeader(theater: Theater) {
        theaterHeader.visibility = View.VISIBLE
        theaterHeader.bind(theater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        val dimens = resources.getDimension(R.dimen.bottom_navigation_height)
        recyclerView.addItemDecoration(SpaceDecorator(lastBottom = dimens.toInt()))
        swipeRefresh.setOnRefreshListener {
            presenter.onRefresh()
        }
        presenter.onViewCreated()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun showError(apiError: ApiError) {
        errorView.show(apiError)
    }

    override fun showError() {
        errorView.show()
    }

    override fun updateAdapter(response: Pair<List<ReservationHistory>, ScreeningsResponseV2>, location: UserLocation?, selected: Pair<Screening, String?>?, segments: List<Int>) {
        when (recyclerView) {
            null -> return@updateAdapter
            else -> recyclerView.visibility = View.VISIBLE
        }
        errorView.visibility = View.GONE
        adapter.data = createData(data =
        adapter.data,
                response = response,
                location = location?.toLocation(),
                selected = selected,
                userSegments = segments
        )
    }

    override fun showRefreshing() {
        when (context) {
            null -> return
            else -> swipeRefresh.isRefreshing = true
        }

    }

    override fun notRefreshing() {
        when (context) {
            null -> return
            else -> swipeRefresh.isRefreshing = false
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
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