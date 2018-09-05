package com.mobile.helpers

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.view.WindowInsets


class CustomRelativeLayout(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private var insetsArray = IntArray(4)


    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        insetsArray[0] = insets.systemWindowInsetLeft
        insetsArray[1] = insets.systemWindowInsetTop
        insetsArray[2] = insets.systemWindowInsetRight
        return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0,
                insets.systemWindowInsetBottom))
    }
}