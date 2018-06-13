package com.mobile.screening

import com.mobile.adapters.ItemSame
import com.mobile.model.Movie
import com.mobile.model.Screening
import com.mobile.model.Theater2
import com.mobile.utils.isValidShowtime

data class ScreeningPresentation(
        val distance:Double? = null,
        val theater:Theater2? = null,
        val screening: Screening? = null,
        val selected: android.util.Pair<Screening, String?>? = null,
        val movie: Movie? = null,
        val type: Int = 0
) : ItemSame<ScreeningPresentation> {

    val enabled: Boolean by lazy {
        movie == null && screening?.approved ?: false
    }

    val hasShowtimes: Boolean by lazy {
        val count = screening?.availabilities
                ?.filter {
                    it.available && isValidShowtime(it.startTime)
                }?.size ?: 0
        count > 0
    }

    override fun sameAs(same: ScreeningPresentation): Boolean {
        val mpId = screening?.moviepassId
        val mpId2 = same.screening?.moviepassId
        if (mpId == null || mpId2 == null) {
            return type == same.type
        }
        return mpId == mpId2;
    }

    override fun contentsSameAs(same: ScreeningPresentation): Boolean {
        val code = hashCode()
        val code2 = same.hashCode()
        return code == code2
    }

}