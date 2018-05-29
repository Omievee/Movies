package com.mobile.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class ParcelableDate(val timeAsString: String? = null, val long: Long? = null) : Date(), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readValue(Long::class.java.classLoader) as? Long) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(timeAsString)
        parcel.writeValue(long)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableDate> {
        override fun createFromParcel(parcel: Parcel): ParcelableDate {
            return ParcelableDate(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableDate?> {
            return arrayOfNulls(size)
        }
    }

}