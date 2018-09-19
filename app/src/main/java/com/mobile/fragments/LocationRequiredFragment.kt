package com.mobile.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.content.ContextCompat
import com.mobile.Constants
import com.mobile.location.LocationManager
import com.mobile.widgets.MPAlertDialog
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

    private val shouldShowDialog: Boolean
        get() {
            return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
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
            true -> {
                if ((!hasFineLocation || !hasCoarseLocation) && !shouldShowDialog) {
                    presenter().onDeniedPermanentLocationPermission()
                } else
                    presenter().onRequestPermissionResult(hasFineLocation || hasCoarseLocation)
            }
        }

    }

    override fun showManuallyGrantPermissions() {
        val context = context ?: return
        MPAlertDialog(context).setMessage(
                R.string.location_permission_required
        ).setPositiveButton(android.R.string.ok) { _, _ ->
            val intent = Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.getPackageName(), null)
            intent.data = uri
            startActivity(intent)
        }.setNegativeButton(R.string.cancel, { _, _ ->

        })
                .show()
    }

    override fun showNeedLocationPermissions() {
        val context = context ?: return
        MPAlertDialog(context).setMessage(
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
        if (showEnableLocation && locationProvider.isLocationEnabled()) {
            showEnableLocation = false
            checkLocationPermissions()
        }
    }

    override fun checkLocationPermissions() {
        when (hasFineLocation && hasCoarseLocation) {
            true -> {
                when (hasLocationEnabled) {
                    false -> return presenter().onDoesNotHaveLocationEnabled()
                    true -> return presenter().onHasLocationPermission()
                }
            }
            false -> presenter().onDoesNotHaveLocationPermission()
        }
    }

}