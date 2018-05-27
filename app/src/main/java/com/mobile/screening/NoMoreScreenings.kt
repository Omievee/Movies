package com.mobile.screening

import android.support.constraint.ConstraintLayout
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.moviepass.R

class NoMoreScreenings(context: Context) : ConstraintLayout(context) {

    init {
        inflate(context, R.layout.layout_no_more_screenings, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT,WRAP_CONTENT)
    }

}
