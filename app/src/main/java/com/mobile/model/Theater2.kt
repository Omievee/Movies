package com.mobile.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Theater2(
        val id: Int? = null,
        val name: String? = null,
        var latitude: Double? = null,
        var longitude: Double? = null,
        val tribuneTheaterId: Int? = null,
        var city:String?=null,
        var state:String?=null,
        var address:String?=null,
        var zip:String?=null
        ) : Parcelable {
    fun toTheater(): Theater {
        return Theater().apply {
            setId(id)
            setName(name)
            setLat(latitude ?: 0.0)
            setLon(longitude ?: 0.0)
            setCity(city)
            setState(state)
            setZip(zip)
            setTribuneTheaterId(tribuneTheaterId?:0)
        }
    }

    val cityStateZip:String?
    get() {
        return "${city}, ${state} ${zip}"
    }
}