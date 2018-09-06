package com.mobile.recycler.decorator

import android.content.Context
import android.util.AttributeSet
import com.mobile.featured.VerticalMoviePosterView
import com.moviepass.R

class HistoryView(context: Context) : VerticalMoviePosterView(context, null) {
    init {
        val padding = resources.getDimension(R.dimen.margin_quarter).toInt()
        setPadding(padding,padding,padding,padding)
        //layoutParams = MarginLayoutParams(determinedWidth, (determinedWidth*3/2f).toInt())
    }


}