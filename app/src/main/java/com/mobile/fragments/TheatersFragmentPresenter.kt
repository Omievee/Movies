package com.mobile.fragments

import com.mobile.location.*
import com.mobile.model.Theater
import com.mobile.theater.*
import io.reactivex.disposables.Disposable

class TheatersFragmentPresenter(val view: TheatersFragmentView, val locationManager: LocationManager, val theaterManager: TheaterManager, val theaterUIManager: TheaterUIManager, val geocoder: Geocoder) {


    var location: UserLocation? = null
    var locationSub: Disposable? = null
    var theaterSub: Disposable? = null
    var searchSub: Disposable? = null
    var geocodeSub: Disposable? = null
    var theaterUISub: Disposable? = null

    fun onDestroy() {
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

    fun onPrimary() {
        when (location == null) {
            true -> checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        view.checkLocationPermissions()
    }

    fun onHasLocationPermission() {
        fetchLocation()
    }

    fun onDoesNotHaveLocationPermission() {
        view.requestLocationPermissions()
    }

    fun onRequestPermissionResult(hasPermissions: Boolean) {
        when (hasPermissions) {
            true -> checkLocationEnabled()
            false -> view.showNeedLocationPermissions()
        }
    }

    private fun checkLocationEnabled() {
        when (locationManager.isLocationEnabled()) {
            true -> fetchLocation()
            false -> view.showEnableLocation()
        }
    }

    private fun fetchLocation() {
        locationSub?.dispose()
        locationSub = locationManager
                .location()
                .subscribe { t1, t2 ->
                    t1?.let {
                        onLocation(it)
                    }
                    t2?.let {
                        //TODO: show error
                    }
                }
    }

    private fun onLocation(it: UserLocation) {
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

    fun onClickPermissionsNeededMessasage() {
        view.requestLocationPermissions()
    }

    fun onMapIconClicked() {
        view.showMap()
    }

    fun onLocationClicked() {
        view.checkLocationPermissions()
    }

    fun onSearchLocation(loc: String) {
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

