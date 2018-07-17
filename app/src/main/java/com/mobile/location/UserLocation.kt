package com.mobile.location

class UserLocation(val lat:Double=0.0, val lon:Double=0.0) {
    companion object {
        val EMPTY: UserLocation = UserLocation()

        const val R = 6_372.8 * 1_000 // in kilometers

        fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val λ1 = Math.toRadians(lat1)
            val λ2 = Math.toRadians(lat2)
            val Δλ = Math.toRadians(lat2 - lat1)
            val Δφ = Math.toRadians(lon2 - lon1)
            return 2 * R * Math.asin(Math.sqrt(Math.pow(Math.sin(Δλ / 2), 2.0) + Math.pow(Math.sin(Δφ / 2), 2.0) * Math.cos(λ1) * Math.cos(λ2)))
        }
    }
}