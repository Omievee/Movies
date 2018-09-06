package com.mobile.screening

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_no_more_screenings.view.*

class NoMoreScreenings(context: Context) : ConstraintLayout(context) {

    init {
        inflate(context, R.layout.layout_no_more_screenings, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    fun bind(type:Type) {
        noMoreScreenings.setText(when(type) {
            Type.THEATER -> R.string.screenings_no_more_screenings
            Type.MOVIE -> R.string.screenings_no_more_screenings
        })
    }

}

enum class Type {
    MOVIE, THEATER
}
