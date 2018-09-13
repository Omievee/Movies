package com.mobile.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.mobile.Constants
import com.mobile.location.LocationManager
import com.moviepass.R
import javax.inject.Inject

abstract class LocationRequiredFragment : MPFragment(), LocationRequiredView {

    abstract fun presenter(): LocationRequiredPresenter

    @Inject
    lateinit var locationProvider: LocationManager

    var showEnableLocation = false

    private val hasFineLocation: Boolean
        get() {
            val context = context ?: return false
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

    private val hasCoarseLocation: Boolean
        get() {
            val context = context ?: return false
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

    private val hasLocationEnabled: Boolean
        get() {
            return locationProvider.isLocationEnabled()
        }

    override fun requestLocationPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        requestPermissions(permissions, Constants.REQUEST_LOCATION_FROM_THEATERS_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode == Constants.REQUEST_LOCATION_FROM_THEATERS_CODE) {
            true -> presenter().onRequestPermissionResult(hasFineLocation && hasCoarseLocation)
        }
    }

    override fun showNeedLocationPermissions() {
        val context = context ?: return
        AlertDialog.Builder(context).setMessage(
                R.string.location_permission_required
        ).setPositiveButton(android.R.string.ok, { _, _ ->
            presenter().onClickPermissionsNeededMessasage()
        }).show()
    }

    override fun showEnableLocation() {
        EnableLocationFragment.newInstance().show(childFragmentManager, "enable_location")
        showEnableLocation = true
    }

    override fun onResume() {
        super.onResume()
        if(showEnableLocation && locationProvider.isLocationEnabled()) {
            showEnableLocation = false
            checkLocationPermissions()
        }
    }

    override fun checkLocationPermissions() {
        when (hasLocationEnabled) {
            false -> return presenter().onDoesNotHaveLocationEnabled()
        }
        when (hasFineLocation && hasCoarseLocation) {
            true -> presenter().onHasLocationPermission()
            false -> presenter().onDoesNotHaveLocationPermission()
        }
    }

}