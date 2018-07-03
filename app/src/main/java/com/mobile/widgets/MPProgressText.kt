package com.mobile.widgets

import android.content.Context
import android.support.annotation.IntegerRes
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_mp_progress.view.*

class MPProgressText(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_mp_text, this)
        setBackgroundResource(R.drawable.button_red_square)
        val array = context.obtainStyledAttributes(attrs, R.styleable.MPProgressText)
        textView.text = array.getText(R.styleable.MPProgressText_mp_text)
        textView.setTextColor(array.getColor(R.styleable.MPProgressText_mp_textColor, ResourcesCompat.getColor(resources, R.color.white, context.theme)))
        array.recycle()
    }

    var progress: Boolean = false
        set(value) {
            progressView.visibility = when (value) {
                true -> {
                    View.VISIBLE}
                false -> {
                    View.INVISIBLE}

            }
            isEnabled = !value
        }
    @IntegerRes var text:Int = 0
    set(value) {
        textView.setText(value)
    }
}