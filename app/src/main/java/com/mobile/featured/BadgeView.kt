package com.mobile.featured

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.TypedValue
import com.moviepass.R

class BadgeView(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    val path = Path()
    val paint:Paint

    val indent:Float

    init {
        setWillNotDraw(false)
        inflate(context, R.layout.badge_view, this)
        indent = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = 0xFFE4333A.toInt()
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path,paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val width = width.toFloat()
        val height = height.toFloat()
        path.reset()
        path.moveTo(0f,0f)
        path.lineTo(width,0f)
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.lineTo(indent, height/2f)
        path.close()
    }
}