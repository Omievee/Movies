package com.mobile.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.android.synthetic.main.fragment_change_billing_and_plan_info.view.*


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
    val p = getNavigationBarSize(c)
    return p.y
}

fun ViewGroup.forEachIndexed(
        action: (index: Int, view: View) -> Unit
) {
    for (i in 0 until this.childCount) {
        action(i, this.getChildAt(i))
    }
}

fun ViewGroup.children(
        predicate: (index:Int, view:View) -> Boolean?
):List<View> {
    val views = mutableListOf<View>()
    for(i in 0 until this.childCount) {
        val view = this.getChildAt(i)
        if(predicate(i,view)!=false) {
            views.add(view)
        }
    }
    return views
}

private fun getNavigationBarSize(context: Context): Point {
    val appUsableSize = getAppUsableScreenSize(context)
    val realScreenSize = getRealScreenSize(context)

    // navigation bar on the side
    if (appUsableSize.x < realScreenSize.x) {
        return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
    }

    // navigation bar at the bottom
    return if (appUsableSize.y < realScreenSize.y) {
        Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
    } else Point()

    // navigation bar is not present
}

private fun getAppUsableScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

private fun getRealScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()

    display.getRealSize(size)

    return size
}

private fun isTablet(c: Context): Boolean {
    return c.getResources().getConfiguration().screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
}
