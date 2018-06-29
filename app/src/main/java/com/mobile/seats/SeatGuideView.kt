package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.view.View
import com.moviepass.R

class SeatGuideView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {
    init {
        inflate(context, R.layout.layout_screen_key_guide, this)
    }

    fun noReserved() {
        val set = ConstraintSet()
        set.clone(this)
        set.setVisibility(R.id.reserved, View.GONE)
        set.connect(R.id.unavailable, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(R.id.unavailable, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(R.id.available, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(R.id.available, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.constrainWidth(R.id.available, ConstraintSet.WRAP_CONTENT)
        set.constrainWidth(R.id.unavailable, ConstraintSet.WRAP_CONTENT)
        set.setHorizontalBias(R.id.available, .25f)
        set.setHorizontalBias(R.id.unavailable, .75f)
        set.applyTo(this)
    }
}