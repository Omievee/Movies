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