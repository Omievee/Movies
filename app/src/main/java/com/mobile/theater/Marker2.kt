package com.mobile.theater

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.TypedValue
import android.widget.ImageView

class Marker2(context: Context) : ImageView(context) {

    val widthy = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22f, resources.displayMetrics).toInt()

    @ColorInt
    val color = 0x88c82229.toInt()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        paint.color = color
        canvas.drawCircle(canvas.width/2f, canvas.height/2f, canvas.height/2f, paint)
        super.onDraw(canvas)
    }

}