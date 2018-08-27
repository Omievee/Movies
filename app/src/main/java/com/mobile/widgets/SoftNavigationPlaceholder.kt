package com.mobile.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mobile.utils.navBarHeight
import com.mobile.utils.padding
import com.moviepass.R

class SoftNavigationPlaceholder(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {

    var showBottom: Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                navBarHeight + when (showBottom) {
                    true -> resources.getDimension(R.dimen.bottom_navigation_height).toInt() + resources.getDimension(R.dimen.margin_standard).toInt()
                    false -> 0
                }, MeasureSpec.EXACTLY))
    }

}