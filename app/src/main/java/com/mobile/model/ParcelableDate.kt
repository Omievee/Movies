package com.mobile.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class ParcelableDate(val timeAsString: String? = null, val timeAsLong: Long? = null) : Date(timeAsLong?:0), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readValue(Long::class.java.classLoader) as? Long) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(timeAsString)
        parcel.writeValue(timeAsLong)
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

val Date.asParcelableDate:ParcelableDate
get() {
    return ParcelableDate(timeAsLong = time)
}
fun ParcelableDate.monthDayYear(): String {
    return java.text.SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(this)

}

