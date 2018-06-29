package com.mobile.screening

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.moviepass.R

class NoMoreScreenings(context: Context) : ConstraintLayout(context) {

    init {
        inflate(context, R.layout.layout_no_more_screenings, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT)

    }

}
