package com.mobile.screening

import com.mobile.adapters.ItemSame
import com.mobile.model.Availability
import com.mobile.model.Screening
import com.mobile.model.Surge

data class ShowtimePresentation(
        val screening: Screening?,
        val availability: Availability?,
        val surge: Surge?
) : ItemSame<ShowtimePresentation> {

    override fun sameAs(same: ShowtimePresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: ShowtimePresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}