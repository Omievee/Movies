package com.mobile.extensions

import android.content.res.Resources
import android.graphics.drawable.StateListDrawable
import com.mobile.drawable.StateDrawableBuilder
import com.mobile.model.SeatInfo
import com.mobile.model.SeatInfo.SeatType.*
import com.moviepass.R

class SeatHelper {

    companion object {
        fun getDrawable(seat: SeatInfo, resources: Resources):StateListDrawable? {
            val builder = when (seat.seatType) {
                SeatTypeNotASeat-> null
                SeatTypeCanReserve,
                SeatTypeUnknown,
                SeatTypeCanReserveLeft,
                SeatTypeCanReserveRight
                -> StateDrawableBuilder(
                        normal = R.drawable.icon_seat_available,
                        disabled = R.drawable.icon_seat_unavailable,
                        selected = R.drawable.icon_seat_selected,
                        resources = resources)

                SeatTypeWheelchair -> StateDrawableBuilder(
                        normal = R.drawable.icon_seat_wheelchair_available,
                        disabled = R.drawable.icon_seat_wheelchair_unavailable,
                        selected = R.drawable.icon_seat_wheelchair_selected,
                        resources = resources)
                SeatTypeCompanion -> StateDrawableBuilder(
                        normal = R.drawable.icon_seat_companion_available,
                        disabled = R.drawable.icon_seat_companion_unavailable,
                        selected = R.drawable.icon_seat_companion_selected,
                        resources = resources)
                SeatTypeSofaLeft -> StateDrawableBuilder(
                        normal = R.drawable.icon_seat_sofa_left_available,
                        disabled = R.drawable.icon_seat_sofa_left_unavailable,
                        selected = R.drawable.icon_seat_sofa_left_selected,
                        resources = resources)
                SeatTypeSofaMiddle -> StateDrawableBuilder(
                        normal = R.drawable.icon_seat_sofa_middle_available,
                        disabled = R.drawable.icon_seat_sofa_middle_unavailable,
                        selected = R.drawable.icon_seat_sofa_middle_selected,
                        resources = resources)
                SeatTypeSofaRight -> StateDrawableBuilder(
                        normal = R.drawable.icon_seat_sofa_right_available,
                        disabled = R.drawable.icon_seat_sofa_right_unavailable,
                        selected = R.drawable.icon_seat_sofa_right_selected,
                        resources = resources)
            }
            return builder?.build()
        }
    }

}