package com.mobile.fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.*
import android.os.Bundle
import android.support.transition.*
import android.support.v4.content.ContextCompat.*
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.Error
import com.mobile.Primary
import com.mobile.keyboard.KeyboardManager
import com.mobile.location.UserLocation
import com.mobile.model.Theater
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.theater.*
import com.moviepass.R
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_theaters2.*
import javax.inject.Inject

class TheatersFragmentV2 : MPFragment(), TheatersFragmentView, Primary {


    @Inject
    lateinit var presenter: TheatersFragmentPresenter

    @Inject
    lateinit var keyboardManager: KeyboardManager

    var mapFragment: TheaterMapFragment? = null

    val clickLocation = object : TheaterClickListener {
        override fun onTheaterClicked(theater: Theater) {
            showFragment(ScreeningsFragment.newInstance(ScreeningsData(theater = theater)))
        }
    }

    var adapter: TheatersAdapterV2 = TheatersAdapterV2(clickLocation)

    override fun setAdapterData(location: UserLocation, theaters: List<Theater>) {
        adapter.data = TheatersAdapterV2.createData(last = adapter.data, userLocation = location, theaters = theaters)
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    private val hasFineLocation: Boolean
        get() {
            val context = context ?: return false
            return checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        }

    private val hasCoarseLocation: Boolean
        get() {
            val context = context ?: return false
            return checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
        }


    override fun requestLocationPermissions() {
        val permissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        requestPermissions(permissions, Constants.REQUEST_LOCATION_FROM_THEATERS_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode == Constants.REQUEST_LOCATION_FROM_THEATERS_CODE) {
            true -> presenter.onRequestPermissionResult(hasFineLocation && hasCoarseLocation)
        }
    }

    override fun showNeedLocationPermissions() {
        val context = context ?: return
        AlertDialog.Builder(context).setMessage(
                R.string.location_permission_required
        ).setPositiveButton(android.R.string.ok, { _, _ ->
            presenter.onClickPermissionsNeededMessasage()
        }).show()
    }

    override fun showEnableLocation() {
        EnableLocation.newInstance().show(childFragmentManager, "enable_location")
    }

    override fun checkLocationPermissions() {
        when (hasFineLocation && hasCoarseLocation) {
            true -> presenter.onHasLocationPermission()
            false -> presenter.onDoesNotHaveLocationPermission()
        }
    }

    override fun onPrimary() {
        presenter.onPrimary()
        when (mapFragment) {
            null -> showMap(true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_theaters2, container, false)
    }

    override fun showMap(invisible: Boolean) {
        when (mapFragment) {
            null -> mapFragment = TheaterMapFragment()
        }
        when (invisible) {
            false -> showMapFragment()
        }
    }

    private fun showMapFragment() {
        val fragment = mapFragment ?: return
        showFragment(fragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        val navBar = resources.getDimension(R.dimen.bottom_navigation_height).toInt()
        val margin = resources.getDimension(R.dimen.margin_standard).toInt()
        val decor = SpaceDecorator(lastBottom = navBar, bottom = margin)
        recyclerView.addItemDecoration(decor)
        recyclerView.addItemDecoration(StickyRecyclerHeadersDecoration(adapter))
        mapSearchBox.listener = object : MapSearchBoxListener {
            override fun onClose() {
                hideSearchBox()
            }

            override fun onSearch(query: String) {
                showProgress()
                presenter.onSearchLocation(query)
                keyboardManager.hide()
                //hideSearchBox()
            }
        }

        mapIcon.apply {
            setOnClickListener {
                presenter.onMapIconClicked()
            }
        }
        searchIcon.setOnClickListener {
            showSearchBox()
        }

        currentLocationContainer.setOnClickListener {
            presenter.onLocationClicked()
            showProgress()
        }

        presenter.onCreate()
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun hideSearchBox() {
        val set = AutoTransition()
        set.duration = 200
        val listener = object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                keyboardManager.hide()
            }
        }
        set.addListener(listener)
        TransitionManager.beginDelayedTransition(theaterListCL, set)
        searchBar.visibility = View.INVISIBLE
        searchIcon.visibility = View.VISIBLE
        currentLocationContainer.visibility = View.GONE
    }

    private fun showSearchBox() {
        TransitionManager.beginDelayedTransition(theaterListCL)
        searchBar.visibility = View.VISIBLE
        searchIcon.visibility = View.INVISIBLE
        currentLocationContainer.visibility = View.VISIBLE
    }

    override fun showNoTheatersFound() {
        errorView.show(ApiError(error = Error(message = resources.getString(R.string.no_theaters_found))))
        recyclerView.visibility = View.GONE
    }

    override fun hideNoTheatersFound() {
        errorView.hide()
        recyclerView.visibility = View.VISIBLE
    }

    override fun showNoLocationFound() {
        errorView.show(ApiError(error = Error(message = resources.getString(R.string.no_theaters_found))))
        recyclerView.visibility = View.GONE
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}