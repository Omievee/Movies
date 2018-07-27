package com.mobile.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class SeatInfo(@SerializedName("available") var isAvailable: Boolean = false,
               var row: Int = 0,
               var column: Int = 0,
               var seatName: String? = null,
               var type: String? = null
) : Parcelable, Comparable<SeatInfo> {

    val seatType: SeatType
        get() {

            var seatType = SeatType.SeatTypeUnknown

            if (type!!.toLowerCase().matches("canreserve".toRegex())) {
                seatType = SeatType.SeatTypeCanReserve
            }
            if (type!!.toLowerCase().matches("canreserveleft".toRegex())) {
                seatType = SeatType.SeatTypeCanReserveLeft
            }
            if (type!!.toLowerCase().matches("canreserveright".toRegex())) {
                seatType = SeatType.SeatTypeCanReserveRight
            } else if (type!!.toLowerCase().matches("wheelchair".toRegex())) {
                seatType = SeatType.SeatTypeWheelchair
            } else if (type!!.toLowerCase().matches("companion".toRegex())) {
                seatType = SeatType.SeatTypeCompanion
            } else if (type!!.toLowerCase().matches("notaseat".toRegex())) {
                seatType = SeatType.SeatTypeNotASeat
            } else if (type!!.toLowerCase().matches("sofaleft".toRegex())) {
                seatType = SeatType.SeatTypeSofaLeft
            } else if (type!!.toLowerCase().matches("sofaright".toRegex())) {
                seatType = SeatType.SeatTypeSofaRight
            } else if (type!!.toLowerCase().matches("sofamiddle".toRegex())) {
                seatType = SeatType.SeatTypeSofaMiddle
            }
            return seatType
        }

    val isWheelChairOrCompanion: Boolean
        get() {
            val type = seatType
            return type == SeatType.SeatTypeWheelchair || type == SeatType.SeatTypeCompanion
        }

    enum class SeatType {
        SeatTypeUnknown,
        SeatTypeCanReserve,
        SeatTypeCanReserveLeft,
        SeatTypeCanReserveRight,
        SeatTypeWheelchair,
        SeatTypeCompanion,
        SeatTypeNotASeat,
        SeatTypeSofaLeft,
        SeatTypeSofaMiddle,
        SeatTypeSofaRight
    }

    override fun compareTo(another: SeatInfo): Int {
        val ss = another

        return if (row == ss.row)
            this.column - ss.column
        else
            this.row - ss.row
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val seatInfo = other as SeatInfo?

        if (row != seatInfo!!.row) return false
        if (column != seatInfo.column) return false
        if (if (seatName != null) seatName != seatInfo.seatName else seatInfo.seatName != null)
            return false
        return if (type != null) type == seatInfo.type else seatInfo.type == null
    }

    override fun hashCode(): Int {
        var result = row
        result = 31 * result + column
        result = 31 * result + if (seatName != null) seatName!!.hashCode() else 0
        result = 31 * result + if (type != null) type!!.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "SeatInfo{" +
                "available=" + isAvailable +
                ", row=" + row +
                ", column=" + column +
                ", seatName='" + seatName + '\''.toString() +
                ", type='" + type + '\''.toString() +
                '}'.toString()
    }

    fun asPosition(): SeatPosition {
        return SeatPosition(row = row, column = column)
    }
}