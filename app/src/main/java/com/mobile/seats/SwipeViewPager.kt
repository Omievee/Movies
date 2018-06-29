package com.mobile.seats

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class SwipeViewPager(context: Context, attrs: AttributeSet?=null) : ViewPager(context, attrs) {

    var canSwipe:Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if(canSwipe) {
            return super.onTouchEvent(ev)
        } else {
            return false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.canSwipe) {
            super.onInterceptTouchEvent(event)
        } else false

    }

}