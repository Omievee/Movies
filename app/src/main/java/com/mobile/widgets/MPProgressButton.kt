package com.mobile.widgets

import android.content.Context
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
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
        textView.text = array.getText(R.styleable.MPProgressButton_mp_text)
        textView.setTextColor(array.getColor(R.styleable.MPProgressButton_mp_textColor, ResourcesCompat.getColor(resources, R.color.white, context.theme)))
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
    @IntegerRes @StringRes var text:Any? = null
    set(value) {
        when(value) {
            is Int -> textView.setText(value)
            is String-> textView.text = value
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = when(enabled) {
            true-> 1f
            false-> .8f
        }
    }
}