package com.mobile.theater

import com.mobile.location.BoundingBox
import com.mobile.location.UserAddress
import com.mobile.location.UserLocation
import com.mobile.model.Theater
import io.reactivex.Observable
import io.reactivex.Single

interface TheaterManager {

    fun theaters(userLocation: UserLocation?=null,box: BoundingBox?=null): Observable<List<Theater>>

    fun theaterLocation(): Observable<UserLocation>

    fun search(address:UserAddress) : Single<List<Theater>>
}