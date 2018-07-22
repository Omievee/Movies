package com.mobile.screenings

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

class PinnedBottomSheetBehavior<V : View>(context: Context?, attrs: AttributeSet?) : BottomSheetBehavior<V>(context, attrs) {

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
}