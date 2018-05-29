package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Theater2(
        val id: Int? = null,
        val name: String? = null,
        var latitude: Double? = null,
        var longitude: Double? = null,
        val tribuneTheaterId: Int? = null) : Parcelable {
    fun toTheater(): Theater {
        return Theater().apply {
            setId(id)
            setName(name)
            setLat(latitude ?: 0.0)
            setLon(longitude ?: 0.0)
            setTribuneTheaterId(tribuneTheaterId?:0)
        }
    }
}