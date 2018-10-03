package com.mobile

import com.mobile.model.SeatPosition

class Error(val code: Int = -1,
            var message: String = "",
            var title: String? = null,
            var error: String? = null,
            var unavailablePositions: List<SeatPosition>?=null) {
}