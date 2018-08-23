package com.mobile.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mobile.utils.navBarHeight
import com.mobile.utils.padding

class SoftNavigationPlaceholder(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                navBarHeight, MeasureSpec.EXACTLY))
    }

}