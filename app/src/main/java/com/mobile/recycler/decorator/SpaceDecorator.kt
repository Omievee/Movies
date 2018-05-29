package com.mobile.recycler.decorator

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpaceDecorator(
        val lastEnd: Int? = null
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        lastEnd?.let {
            val position = parent.getChildAdapterPosition(view)
            if (state.itemCount - 1 == position) {
                outRect.right = it
            }
        }
    }

}