package com.mobile.model

import android.os.Parcelable
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class Theater(
        @PrimaryKey
        var id: Int = 0,
        var name: String? = null,
        var lat: Double = 0.0,
        var lon: Double = 0.0,
        var tribuneTheaterId: Int? = null,
        var distance:Double = 0.0,
        var city: String? = null,
        var state: String? = null,
        var address: String? = null,
        var zip: String? = null,
        var ticketType: String? = null,
        var moviepassId:Int = 0
) : RealmModel, Parcelable {

    fun ticketTypeIsStandard(): Boolean {
        return ticketType?.matches("STANDARD".toRegex()) == true
    }

    fun ticketTypeIsETicket(): Boolean {
        return ticketType?.matches("E_TICKET".toRegex()) == true
    }

    fun ticketTypeIsSelectSeating(): Boolean {
        return ticketType?.matches("SELECT_SEATING".toRegex()) == true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Theater

        if (id != other.id) return false
        if (name != other.name) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false
        if (tribuneTheaterId != other.tribuneTheaterId) return false
        if (distance != other.distance) return false
        if (city != other.city) return false
        if (state != other.state) return false
        if (address != other.address) return false
        if (zip != other.zip) return false
        if (ticketType != other.ticketType) return false
        if (moviepassId != other.moviepassId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        result = 31 * result + (tribuneTheaterId ?: 0)
        result = 31 * result + distance.hashCode()
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (zip?.hashCode() ?: 0)
        result = 31 * result + (ticketType?.hashCode() ?: 0)
        result = 31 * result + moviepassId
        return result
    }

    val cityStateZip: String?
        get() {
            return "${city}, ${state} ${zip}"
        }
}

