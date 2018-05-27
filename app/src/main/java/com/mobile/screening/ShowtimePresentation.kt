package com.mobile.screening

import com.mobile.adapters.ItemSame
import com.mobile.model.Screening

data class ShowtimePresentation(val screening: Screening?, val showtime: String?) : ItemSame<ShowtimePresentation> {

    override fun sameAs(same: ShowtimePresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: ShowtimePresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}