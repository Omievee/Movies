package com.mobile.screenings

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PinnedBottomSheetBehavior<V : View>(context: Context?, attrs: AttributeSet?) : BottomSheetBehavior<V>(context, attrs) {

    var locked:Boolean = false

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return when {
            child is PinToAppBar && dependency is AppBarLayout -> true
            else -> false
        }
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return when (dependency.measuredHeight != parent.measuredHeight-peekHeight) {
            true -> {
                peekHeight = parent.measuredHeight-dependency.measuredHeight
                child.minimumHeight = peekHeight
                true
            }
            false -> false
        }
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent?): Boolean {
        var handled = false

        if (!locked) {
            handled = super.onInterceptTouchEvent(parent, child, event)
        }

        return handled
    }

    override fun onTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent?): Boolean {
        var handled = false

        if (!locked) {
            handled = super.onTouchEvent(parent, child, event)
        }

        return handled
    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        if(locked) {
            return false
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        if(locked) {
            return false
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if(locked) {
            return
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        if(locked) {
            return
        }
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        if(locked) {
            return false
        }
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, type: Int) {
        if(locked) {
            return
        }
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
    }
}