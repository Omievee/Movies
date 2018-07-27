package com.mobile.fragments

import com.mobile.analytics.AnalyticsManager
import com.mobile.location.*
import com.mobile.model.Theater
import com.mobile.theater.*
import io.reactivex.disposables.Disposable

class TheatersFragmentPresenter(override val view: TheatersFragmentView, locationManager: LocationManager, val theaterManager: TheaterManager, val theaterUIManager: TheaterUIManager, val geocoder: Geocoder, val analyticsManager: AnalyticsManager) : LocationRequiredPresenter(view, locationManager) {

    var theaterSub: Disposable? = null
    var searchSub: Disposable? = null
    var geocodeSub: Disposable? = null
    var theaterUISub: Disposable? = null

    public override fun onDestroy() {
        super.onDestroy()
        locationSub?.dispose()
        theaterSub?.dispose()
        theaterUISub?.dispose()
        theaterUIManager.cleanup()
    }

    fun onCreate() {
        subscribe()
    }

    private fun subscribe() {
        theaterUISub = theaterUIManager.mappedTheaters()
                .subscribe({ theaters ->
                    view.scrollToTop()
                    view.setAdapterData(theaters.location, theaters.theaters)
                }, {

                })

    }


    override fun onLocation(it: UserLocation) {
        location = it
        fetchNearbyTheaters(it)
    }

    private fun fetchNearbyTheaters(loc: UserLocation) {
        view.showProgress()
        theaterSub = theaterManager
                .theaters(loc)
                .doAfterTerminate { view.hideProgress() }
                .subscribe({ theaters ->
                    onTheaters(loc, theaters)
                }, { error ->
                    error.printStackTrace()
                })
    }

    fun onMapIconClicked() {
        view.showMap()
        analyticsManager.onTheaterMapOpened()
    }

    fun onLocationClicked() {
        view.checkLocationPermissions()
    }

    fun onSearchLocation(loc: String) {
        analyticsManager.onTheaterSearch(loc)
        geocodeSub?.dispose()
        geocodeSub = geocoder.reverseLocation(loc)
                .subscribe({
                    onGeocode(it)
                }, {
                    view.hideProgress()
                    when (it is NoLocationFoundException) {
                        true -> view.showNoLocationFound()
                    }
                    it.printStackTrace()
                })
    }

    private fun onGeocode(it: UserAddress) {
        searchSub?.dispose()
        searchSub = theaterManager.search(it)
                .doAfterTerminate {
                    view.hideProgress()
                }
                .subscribe({ loc ->
                    view.scrollToTop()
                    onTheaters(it.location, loc)
                }, {
                    it.printStackTrace()
                })
    }

    private fun onTheaters(location: UserLocation, theaters: List<Theater>) {
        view.setAdapterData(location, theaters)
        if (theaters.isEmpty()) {
            view.showNoTheatersFound()
        } else {
            view.hideNoTheatersFound()
        }
        theaterUIManager.listTheaters(TheatersPayload(location, theaters))
    }
}

