package com.mobile.seats

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moviepass.R

class MPProgress(context: Context?, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.layout_progress, this)
    }
}