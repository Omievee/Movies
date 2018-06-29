package com.mobile.seats

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import com.mobile.extensions.CustomSwipeRefresh
import com.moviepass.R

class MPSwipeRefreshLayout(context: Context, attrs: AttributeSet? = null) : CustomSwipeRefresh(context, attrs) {
    init {
        setColorSchemeResources(R.color.red, R.color.red_dark)
    }
}