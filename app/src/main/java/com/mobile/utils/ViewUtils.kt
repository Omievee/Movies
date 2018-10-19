package com.mobile.utils

import android.view.View
import android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.R.attr.orientation
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.KeyEvent.KEYCODE_BACK
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import com.mobile.MPActivty
import com.moviepass.R


fun View.padding(
        start: Int = paddingStart,
        top: Int = paddingTop,
        bottom: Int = paddingBottom,
        end: Int = paddingEnd) {
    this.setPadding(
            start, top, bottom, end
    )
}

fun View.paddingDp(
        start: Int = paddingStart,
        top: Int = paddingTop,
        bottom: Int = paddingBottom,
        end: Int = paddingEnd) {
    this.setPadding(
            asDp(start, resources), asDp(top, resources), asDp(bottom, resources), asDp(end, resources)
    )
}

fun asDp(dimen: Int, resources: Resources): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimen.toFloat(), resources.displayMetrics).toInt()
}

val View.navBarHeight: Int
    get() {
        return getNavBarHeight(context)
    }

fun getNavBarHeight(c: Context): Int {
    var result = 0
    val hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey()
    val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)

    if (MPActivty.isEmulator || (!hasMenuKey && !hasBackKey) || MPActivty.isEssentialPhone) {
        //The device has a navigation bar
        val resources = c.resources

        return resources.getDimension(R.dimen.bottom_navigation_height).toInt()
    }
    return result
}

private fun isTablet(c: Context): Boolean {
    return c.getResources().getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
}
