package com.mobile.screening

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.model.Availability
import com.mobile.model.Surge
import com.mobile.model.SurgeType
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_showtime.view.*

class ShowtimeView(context: Context, attr: AttributeSet? = null) : ConstraintLayout(context, attr) {

    var time: String? = null

    init {
        View.inflate(context, R.layout.layout_showtime, this)
    }

    fun bind(availability: Availability, surge:Surge, screening: ScreeningPresentation) {
        this.time = availability.startTime
        text.text = time
        when (screening.selected?.second == availability.startTime) {
            true -> isSelected = true
            else -> isSelected = false
        }
        isEnabled = screening.enabled
        surgeIcon.visibility = when (surge.level) {
            SurgeType.NO_SURGE -> View.INVISIBLE
            else -> when(screening.enabled) {
                true-> View.VISIBLE
                else-> View.INVISIBLE
            }
        }
        surgeIcon.isEnabled = when (surge.level) {
            SurgeType.SURGING -> true
            else -> false
        }

    }
}