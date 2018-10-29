package com.mobile.fragments

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.Error
import com.mobile.UserPreferences
import com.mobile.adapters.MissingCheckinListener
import com.mobile.adapters.ScreeningsAdapter
import com.mobile.adapters.ScreeningsAdapter.Companion.createData
import com.mobile.history.model.ReservationHistory
import com.mobile.listeners.BonusMovieClickListener
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.location.UserLocation
import com.mobile.model.AmcDmaMap
import com.mobile.model.Movie
import com.mobile.model.Screening
import com.mobile.model.Theater
import com.mobile.peakpass.PeakPassActivity
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.reservation.Checkin
import com.mobile.responses.ScreeningsResponseV2
import com.mobile.screening.MoviePosterClickListener
import com.mobile.screenings.PinnedBottomSheetBehavior
import com.mobile.seats.MPBottomSheetFragment
import com.mobile.seats.SheetData
import com.mobile.surge.PeakPricingActivity
import com.mobile.utils.highestElevation
import com.mobile.utils.isComingSoon
import com.mobile.utils.showTheaterBottomSheetIfNecessary
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_screenings.*
import javax.inject.Inject

class ScreeningsFragment : LocationRequiredFragment(), ShowtimeClickListener, MissingCheckinListener, ScreeningsFragmentView, BonusMovieClickListener {
    override fun onBonusBannerClickListener() {
        MPBottomSheetFragment.newInstance(SheetData(
                title = resources.getString(R.string.bonus_movies_title),
                description = resources.getString(R.string.bonus_movies_description)
        )).show(childFragmentManager, "")
    }

    @Inject
    lateinit var presenter: ScreeningsFragmentPresenter

    @Inject
    lateinit var dataMap:AmcDmaMap


    val adapter: ScreeningsAdapter = ScreeningsAdapter(this, this, this)

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

    override fun setMovieHeader(movie: Movie, synopsisListener: Boolean, showWhiteListBanner: Boolean) {
        movieHeader.visibility = View.VISIBLE
        when (synopsisListener) {
            true -> movieHeader.bind(movie, this.synopsislistener, showWhiteListBanner,this)
            false -> movieHeader.bind(movie, null, showWhiteListBanner,this)
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
        recyclerView.itemAnimator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
        swipeRefresh.setOnRefreshListener {
            presenter.onRefresh()
        }
        presenter.onViewCreated()
    }

    override fun setRecyclerSpacing(topSpacing:Boolean) {
        val dimens = resources.getDimension(R.dimen.bottom_navigation_height)
        val top = when(topSpacing) {
            true-> resources.getDimension(R.dimen.margin_half)
            false-> 0f
        }
        recyclerView.addItemDecoration(
                SpaceDecorator(
                        lastBottom = dimens.toInt(),
                        firstTop = top.toInt()
                ))
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun showNoMoreScreenings() {
        activity?:return
        errorView.show(ApiError(
                error = Error(
                        message = getString(R.string.screenings_no_more_screenings)
                )
        ))
    }

    override fun showError(apiError: ApiError) {
        activity?:return
        errorView.show(apiError)
    }

    override fun showError() {
        activity?:return
        errorView.show()
    }

    override fun onBack(): Boolean {
        val bottom = BottomSheetBehavior.from(synopsisBottomSheetView) as? PinnedBottomSheetBehavior
        return when(bottom?.locked) {
            null -> super.onBack()
            true -> super.onBack()
            false -> {
                when(bottom.state!=BottomSheetBehavior.STATE_HIDDEN) {
                    true-> {
                        hideSynopsis()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun updateAdapter(response: Pair<List<ReservationHistory>, ScreeningsResponseV2>, location: UserLocation?, selected: Pair<Screening, String?>?, segments: List<Int>) {
        context ?: return
        recyclerView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        adapter.data = createData(data =
        adapter.data,
                response = response,
                location = location?.toLocation(),
                selected = selected,
                userSegments = segments,
                dataMap = dataMap
        )
    }

    override fun showRefreshing() {
        context ?: return
        swipeRefresh.isRefreshing = true
    }

    override fun notRefreshing() {
        context ?: return
        swipeRefresh.isRefreshing = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_screenings, container, false)
    }

    override fun surgeInterstitialFlow(screening: ScreeningsResponseV2, lastCode: Int) {
        val activity = activity ?: return
        if (lastCode != Constants.SURGE_INTERSTITIAL_CODE && !UserPreferences.shownPeakPricing && screening.isSurging(UserPreferences.restrictions.userSegments)) {
            startActivityForResult(PeakPricingActivity.newInstance(activity), Constants.SURGE_INTERSTITIAL_CODE)
        } else if (UserPreferences.showPeakPassOnboard && screening.isSurging(UserPreferences.restrictions.userSegments)) {
            showPeakPassActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode)
    }

    private fun showPeakPassActivity() {
        val activity = activity ?: return
        startActivityForResult(PeakPassActivity.newInstance(activity), Constants.PEAK_PASS_INTERSTITIAL)
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