package com.mobile.fragments

import com.mobile.location.LocationManager
import com.mobile.location.UserLocation
import com.mobile.rx.Schedulers
import io.reactivex.disposables.Disposable

abstract class LocationRequiredPresenter(open val view: LocationRequiredView, val locationManager: LocationManager) {

    var location: UserLocation? = null
    var locationSub: Disposable? = null
    var disposable: Disposable? = null

    open fun onPrimary() {
        when (location == null) {
            true -> {
                checkLocationPermissions()
            }
        }
    }

    private fun checkLocationPermissions() {
        view.checkLocationPermissions()
    }

    fun onClickPermissionsNeededMessasage() {
        view.requestLocationPermissions()
    }


    fun onHasLocationPermission() {
        fetchLocation()
    }

    fun onDoesNotHaveLocationPermission() {
        view.requestLocationPermissions()
    }

    fun onDoesNotHaveLocationEnabled() {
        view.showEnableLocation()
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
                .compose(Schedulers.singleDefault())
                .subscribe({ it ->
                    location = it
                    onLocation(it)

                }, { t2 ->
                    t2.printStackTrace()
                })
    }

    open fun onDestroy() {
        locationSub?.dispose()
        disposable?.dispose()
    }

    abstract fun onLocation(userLocation: UserLocation)
    fun onDeniedPermanentLocationPermission() {
        view.showManuallyGrantPermissions()
    }

}