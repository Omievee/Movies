package com.mobile.responses

import com.mobile.model.Screening
import com.mobile.model.Theater

data class ScreeningsResponseV2(val screenings: List<Screening>?=null, val theaters: List<Theater>?=null) {
    fun getTheater(screening: Screening) :Theater? {
        return theaters?.firstOrNull {
            it?.tribuneTheaterId == screening?.tribuneTheaterId
        }
    }
}