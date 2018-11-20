package com.mobile.location

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Looper
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.*
import com.mobile.MPActivty
import com.mobile.application.Application
import com.mobile.rx.Schedulers
import com.moviepass.BuildConfig
import io.reactivex.*
import io.reactivex.Single.create
import io.reactivex.android.schedulers.AndroidSchedulers

class LocationManagerImpl(val application: Application, val systemLocationManager: android.location.LocationManager, val fused: FusedLocationProviderClient) : LocationManager {

    private var _lastLocation: UserLocation? = null

    init {
        if (MPActivty.isEmulator) {
            _lastLocation = BuildConfig.USER_LOCATION
        }
    }

    override fun isLocationEnabled(): Boolean {
        return systemLocationManager
                .isProviderEnabled(GPS_PROVIDER)
                ||
                systemLocationManager
                        .isProviderEnabled(NETWORK_PROVIDER)
    }

    override fun lastLocation(): UserLocation? {
        return _lastLocation
    }

    private var permission: Boolean = false
        get() {
            return ContextCompat.checkSelfPermission(application, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(application, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

    @SuppressLint("MissingPermission")
    override fun location(): Single<UserLocation> {
        if (MPActivty.isEmulator) {
            return Single.just(_lastLocation)
        }
        return updatingLocation(true, 0, 100)
    }

    @SuppressLint("MissingPermission")
    override fun updatingLocation(single: Boolean, timeToWait: Int, minDistanceInMeters: Int): Single<UserLocation> {
        val updates = LocationUpdates(single, permission, fused, LocationRequest().apply {
            this.smallestDisplacement = minDistanceInMeters.toFloat()
            this.fastestInterval = timeToWait.toLong()
            this.maxWaitTime
            if (single) {
                this.numUpdates = 1
            }
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        })
        val observ: Single<UserLocation> = Single.create(updates)
        return observ.map {
            _lastLocation = it
            it
        }.doAfterTerminate { updates.dispose() }.compose(Schedulers.singleDefault())
    }
}

fun Location?.toLocation(): UserLocation {
    this?.let {
        return UserLocation(lat = latitude, lon = longitude)
    } ?: return UserLocation.EMPTY
}

class LocationUpdates(val single: Boolean, val permission: Boolean, val fused: FusedLocationProviderClient, val locationRequest: LocationRequest) : SingleOnSubscribe<UserLocation> {

    lateinit var emitter: SingleEmitter<UserLocation>

    var emitted: Int = 0

    val canEmit: Boolean
        get() {
            return !single || emitted == 0
        }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
        }

        override fun onLocationResult(locationResult: LocationResult) {
            when (emitter.isDisposed == false && canEmit) {
                true -> {
                    val last = locationResult.lastLocation
                    val loc = locationResult.lastLocation.toLocation()
                    emitter.onSuccess(loc)
                    emitted += 1
                }
                false -> {
                    fused.removeLocationUpdates(this)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun subscribe(emitter: SingleEmitter<UserLocation>) {
        this.emitter = emitter
        if (!permission) {
            when (emitter.isDisposed) {
                false -> {
                    emitter.onError(LocationPermission())
                    return
                }
            }
        }
        fused.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun dispose() {
        fused.removeLocationUpdates(locationCallback)
    }
}

class LocationPermission : Throwable("Location permission missing")