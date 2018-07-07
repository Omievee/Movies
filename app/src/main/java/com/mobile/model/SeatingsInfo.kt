package com.mobile.model

/**
 * Created by anubis on 6/27/17.
 */

class SeatingsInfo(val rows: Int = 0, val columns: Int = 0, val seats: List<SeatInfo>? = null) {
    val hasNoSeats: Boolean
        get() {
            return rows == 0 || columns == 0 || seats?.size ?: 0 == 0
        }

}
