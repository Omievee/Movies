package com.mobile.theater

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.mobile.model.Theater

class TheaterPin(val theater: Theater) : ClusterItem {

    val latLng by lazy {
        LatLng(theater.lat, theater.lon)
    }

    override fun getSnippet(): String {
        return theater.name ?: ""
    }

    override fun getTitle(): String {
        return theater.name ?: "Unknown"
    }

    override fun getPosition(): LatLng {
        return latLng
    }

}