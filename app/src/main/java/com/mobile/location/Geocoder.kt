package com.mobile.location

import io.reactivex.Single

interface Geocoder {

    fun reverseLocation(query:String) : Single<UserAddress>
}