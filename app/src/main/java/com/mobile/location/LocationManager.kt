package com.mobile.location

import io.reactivex.Observable
import io.reactivex.Single

interface LocationManager {

    fun isLocationEnabled():Boolean

    fun lastLocation():UserLocation?

    fun location(): Single<UserLocation>

    fun updatingLocation(single:Boolean, timeToWait:Int=10000,minDistanceInMeters:Int=20): Single<UserLocation>

}

