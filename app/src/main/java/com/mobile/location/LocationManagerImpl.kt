package com.mobile.location

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.mobile.application.Application
import com.mobile.rx.Schedulers
import io.reactivex.*
import io.reactivex.Single.create

class LocationManagerImpl(val application: Application, val fused: FusedLocationProviderClient?) : LocationManager {

    private var _lastLocation: UserLocation? = null

    init {
        location().compose(Schedulers.singleDefault())
                .subscribe({
                    _lastLocation = it
                }, {})
    }

    override fun lastLocation(): UserLocation? {
        return _lastLocation
    }

    private var permission: Boolean = false
        get() {
            return ContextCompat.checkSelfPermission(application, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

    @SuppressLint("MissingPermission")
    override fun location(): Single<UserLocation> {
        return create({ emitter ->
            val permission = permission
            if (!permission) {
                when (emitter.isDisposed) {
                    false -> {
                        emitter.onError(LocationPermission())
                    }
                }
                return@create
            }
            val task = fused?.lastLocation
            task?.addOnSuccessListener { location ->
                if (emitter.isDisposed) {
                    return@addOnSuccessListener
                }
                emitter.onSuccess(location.toLocation())
            }
            task?.addOnFailureListener {
                emitter.onError(it)
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun updatingLocation(timeToWait: Int, minDistanceInMeters: Int): Observable<UserLocation> {
        val updates = LocationUpdates(permission, fused, LocationRequest().apply {
            this.smallestDisplacement = minDistanceInMeters.toFloat()
            this.fastestInterval = timeToWait.toLong()
        })
        val observ: Observable<UserLocation> = Observable.create(updates)
        observ.doOnDispose {
            updates.dispose()
        }
        return observ
    }
}

fun Location?.toLocation(): UserLocation {
    this?.let {
        return UserLocation(lat = latitude, lon = longitude)
    } ?: return UserLocation.EMPTY

}

class LocationUpdates(val permission: Boolean, val fused: FusedLocationProviderClient?, val locationRequest: LocationRequest) : ObservableOnSubscribe<UserLocation> {

    var emitter: ObservableEmitter<UserLocation>? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            when (emitter?.isDisposed) {
                false -> locationResult?.let {
                    emitter?.onNext(locationResult.lastLocation.toLocation())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun subscribe(emitter: ObservableEmitter<UserLocation>) {
        this.emitter = emitter
        val permission = permission
        if (!permission) {
            when (emitter.isDisposed) {
                false -> {
                    emitter.onError(LocationPermission())
                    return
                }
            }
        }
        fused?.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun dispose() {
        fused?.removeLocationUpdates(locationCallback)
    }

}

class LocationPermission : Throwable("Location permission missing")