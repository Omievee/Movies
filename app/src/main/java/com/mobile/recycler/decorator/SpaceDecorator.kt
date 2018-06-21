package com.mobile.recycler.decorator

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpaceDecorator(
        val top: Int? = null,
        val start: Int? = null,
        val bottom: Int? = null,
        val end: Int? = null,
        val lastEnd: Int? = null,
        val lastBottom: Int? = null
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        end?.let {
            outRect.right += it
        }
        start?.let {
            outRect.left += it
        }
        top?.let {
            outRect.top += it
        }
        bottom?.let {
            outRect.bottom += it
        }
        lastEnd?.let {
            if (state.itemCount - 1 == position) {
                outRect.right += it
            }
        }
        lastBottom?.let {
            if (state.itemCount - 1 == position) {
                outRect.bottom += it
            }
        }
    }

}