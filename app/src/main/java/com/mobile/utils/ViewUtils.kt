package com.mobile.utils

import android.view.View

fun View.padding(
        start:Int = paddingStart,
        top:Int = paddingTop,
        bottom:Int = paddingBottom,
        end:Int = paddingEnd) {
    this.setPadding(
            start, top, bottom, end
    )
}

val View.navBarHeight:Int
get() {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}