package com.mobile.fragments

import com.mobile.Primary

interface LocationRequiredView : Primary {

    fun checkLocationPermissions()
    fun requestLocationPermissions()
    fun showNeedLocationPermissions()
    fun showEnableLocation()
    fun showProgress()
    fun hideProgress()

}