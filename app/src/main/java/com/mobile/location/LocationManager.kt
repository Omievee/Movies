package com.mobile.location

import io.reactivex.Observable
import io.reactivex.Single

interface LocationManager {

    fun lastLocation():UserLocation?

    fun location(): Single<UserLocation>

    fun updatingLocation(timeToWait:Int=10000,minDistanceInMeters:Int=20): Observable<UserLocation>

}

class UserLocation(val lat:Double=0.0, val lon:Double=0.0) {
    companion object {
        val EMPTY:UserLocation = UserLocation()
    }
}