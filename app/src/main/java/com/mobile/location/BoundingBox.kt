package com.mobile.location

import com.google.android.gms.maps.model.LatLng

class BoundingBox(location: UserLocation, radius: Double?=null, southWest:UserLocation?=null, northEast:UserLocation?=null) {

    private val radiusInMiles = radius?.times(0.000621371)

    val northEast: UserLocation by lazy {
        if(northEast!=null) {
            northEast
        } else if (radiusInMiles!=null) {
            val lon = location.lon + radiusInMiles / Math.abs(Math.cos(Math.toRadians((location.lat))) * 69)
            val lat = location.lat + (radiusInMiles / 69)
            UserLocation(lat = lat, lon = lon)
        } else {
            UserLocation.EMPTY
        }

    }

    fun contains(lat:Double, lon:Double):Boolean {
        return southWest.lat <= lat && lat <= this.northEast.lat && this.containsLng(lon)
    }

    private fun containsLng(lng:Double):Boolean {
        return if (this.southWest.lon <= this.northEast.lon) {
            this.southWest.lon <= lng && lng <= this.northEast.lon
        } else {
            this.southWest.lon <= lng || lng <= this.northEast.lon
        }
    }

    val center:UserLocation by lazy {
        val var1 = (this.southWest.lat + this.northEast.lat) / 2.0
        val var3 = this.northEast.lon
        val var5 = this.southWest.lon
        val var7: Double
        if (this.southWest.lon <= var3) {
            var7 = (var3 + var5) / 2.0
        } else {
            var7 = (var3 + 360.0 + var5) / 2.0
        }
        UserLocation(var1, var7)
    }

    val southWest: UserLocation by lazy {
        if(southWest!=null) {
            southWest
        } else if (radiusInMiles!=null) {
            val lon = location.lon - radiusInMiles / Math.abs(Math.cos(Math.toRadians((location.lat))) * 69)
            val lat = location.lat - (radiusInMiles / 69)
            UserLocation(lat = lat, lon = lon)
        } else {
            UserLocation.EMPTY
        }
    }
}