package com.mobile.featured

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.View


class IndicatorsRecyclerViewItemDecoration(val size: Int? = -1) : RecyclerView.ItemDecoration() {

    private val topPadding = (DP * 16).toInt()

    private val circleRadius = DP * 4

    private val margin = DP * 5

    private val paint = Paint()

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val itemCount =
                size?.let {
                    when (it == -1) {
                        true -> parent.layoutManager.itemCount
                        false -> it
                    }
                } ?: parent.layoutManager.itemCount

        val circleDiameter = circleRadius * 2

        when (itemCount > 1) {
            true -> {
                val totalLength = (circleDiameter * (itemCount - 1)) + ((itemCount - 1) * margin)
                val indicatorStartX = (parent.width - totalLength) / 2f

                val indicatorPosY = parent.height - topPadding / 2f

                drawUnselectedCircles(c, indicatorStartX, indicatorPosY, itemCount)

                val layoutManager = parent.layoutManager as LinearLayoutManager
                var activePosition = layoutManager.findFirstVisibleItemPosition()
                activePosition = getRealPosition(activePosition, itemCount)
                if (activePosition == RecyclerView.NO_POSITION) {
                    return
                }
                drawSelectedCircle(c, indicatorStartX, indicatorPosY, activePosition)
            }
            false -> return
        }

        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }
    }

    private fun getRealPosition(positionInList: Int, itemCount: Int): Int {
        return positionInList % itemCount
    }

    private fun drawUnselectedCircles(c: Canvas, indicatorStartX: Float, indicatorPosY: Float, itemCount: Int) {
        paint.color = Color.GRAY

        val itemWidth = (circleRadius * 2) + margin

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            c.drawCircle(start, indicatorPosY, circleRadius, paint)
            start += itemWidth
        }
    }

    private fun drawSelectedCircle(c: Canvas, indicatorStartX: Float, indicatorPosY: Float, highlightPosition: Int) {
        paint.color = Color.RED

        val itemWidth = (circleRadius * 2) + margin

        val highlightStart = indicatorStartX + itemWidth * highlightPosition
        c.drawCircle(highlightStart, indicatorPosY, circleRadius, paint)

    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = topPadding
    }

    companion object {
        private val DP = Resources.getSystem().displayMetrics.density
    }
}