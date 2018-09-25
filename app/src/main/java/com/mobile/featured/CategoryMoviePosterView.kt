package com.mobile.featured

import android.content.Context
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.util.TypedValue
import com.mobile.utils.paddingDp
import kotlinx.android.synthetic.main.layout_vertical_movie_poster.view.*

class CategoryMoviePosterView(context: Context?, attrs: AttributeSet? = null) : VerticalMoviePosterView(context, attrs) {

    init {
        val aspectW = 2
        val aspectH = 3

        val width = resources.displayMetrics.widthPixels
        val finalWidth = width / 2.85
        val height = finalWidth * aspectH / aspectW

        val w = Math.round(finalWidth).toInt()
        val tenDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10f,resources.displayMetrics).toInt()
        val h = Math.round(height).toInt() + tenDp

        layoutParams = MarginLayoutParams(w, h)
        val set = ConstraintSet()
        set.clone(this)
        set.setMargin(ticket_top_red_dark.id,ConstraintSet.TOP, tenDp)
        set.applyTo(this)
    }

}