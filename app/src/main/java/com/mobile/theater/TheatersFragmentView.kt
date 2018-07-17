package com.mobile.theater

import com.mobile.location.UserLocation
import com.mobile.model.Theater

interface TheatersFragmentView {

    fun checkLocationPermissions()
    fun requestLocationPermissions()
    fun showNeedLocationPermissions()
    fun showEnableLocation()
    fun setAdapterData(location: UserLocation, theaters: List<Theater>)
    fun showMap(invisible:Boolean=false)
    fun showProgress()
    fun hideProgress()

}