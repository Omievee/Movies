package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_mp_progress.view.*

class MPProgressButton(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_mp_progress, this)
        setBackgroundResource(R.drawable.button_red)
        val array = context.obtainStyledAttributes(attrs, R.styleable.MPProgressButton)
        text.text = array.getText(R.styleable.MPProgressButton_mp_text)
        text.setTextColor(array.getColor(R.styleable.MPProgressButton_mp_textColor, ResourcesCompat.getColor(resources, R.color.white, context.theme)))
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

}