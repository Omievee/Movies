package com.mobile.location

import android.location.Address
import com.mobile.ApiError
import com.mobile.rx.Schedulers
import io.reactivex.Single
import kotlin.math.sin

class GeocoderImpl(val geocoder: android.location.Geocoder) : Geocoder {

    override fun reverseLocation(query: String): Single<UserAddress> {
        val single:Single<UserAddress> =  Single.create {
            try {
                val results = geocoder.getFromLocationName(query, 1)
                if(it.isDisposed) {
                    return@create
                }
                when (results.isNotEmpty()) {
                    true -> it.onSuccess(results.first().toUserAddress())
                    false -> it.onError(NoLocationFoundException())
                }
            } catch (e:Error) {
                if(!it.isDisposed) {
                    it.onError(e)
                }
            }
        }
        return single.compose(Schedulers.singleDefault())
    }
}

fun Address.toUserAddress(): UserAddress {
    return UserAddress(
            location = UserLocation(lat = latitude, lon = longitude),
            address = (0 until maxAddressLineIndex).map { getAddressLine(it) }.joinToString("\n"),
            city = subAdminArea,
            state = adminArea,
            zip = postalCode
    )
}