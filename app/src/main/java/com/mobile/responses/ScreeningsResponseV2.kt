package com.mobile.responses

import com.mobile.model.Screening
import com.mobile.model.Theater

data class ScreeningsResponseV2(val screenings: List<Screening>? = null, val theaters: List<Theater>? = null) {

    fun mapMoviepassId(id: Int) {
        screenings
                ?.forEach {
                    it.moviepassId = id
                }
    }

    fun isSurging(userMap: List<Int>): Boolean {
        return screenings?.any {
            it.isSurging(userMap)
        } ?: false
    }
}