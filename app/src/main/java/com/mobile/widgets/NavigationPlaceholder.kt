package com.mobile.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mobile.utils.navBarHeight
import com.moviepass.R

class NavigationPlaceholder(context: Context?, attrs: AttributeSet?=null) : View(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dimen = resources.getDimension(R.dimen.bottom_navigation_height).toInt()*2
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(dimen,MeasureSpec.EXACTLY))
    }
}