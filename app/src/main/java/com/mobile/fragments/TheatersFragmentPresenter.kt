package com.mobile.fragments

import com.mobile.location.Geocoder
import com.mobile.location.LocationManager
import com.mobile.location.UserAddress
import com.mobile.location.UserLocation
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
        run {
            theaterUISub = theaterUIManager.listTheaters()
                    .subscribe({ theaters ->
                        view.setAdapterData(theaters.location, theaters.theaters)
                    }, {

                    })
        }
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
                    view.setAdapterData(loc, theaters)
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
                    view.setAdapterData(it.location, loc)
                    theaterUIManager.listTheaters(TheatersPayload(it.location, loc))
                }, {
                    it.printStackTrace()
                })
    }
}

