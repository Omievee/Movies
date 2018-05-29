package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_mp_switch.view.*

class MPSwitchView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.layout_mp_switch, this)
        val array = context.obtainStyledAttributes(attrs, R.styleable.MPSwitchView)
        text.text = array.getText(R.styleable.MPSwitchView_mp_text)
        checkbox.isChecked = array.getBoolean(R.styleable.MPSwitchView_mp_checked, false)
        text.setTextColor(array.getColor(R.styleable.MPSwitchView_mp_textColor, ResourcesCompat.getColor(resources, R.color.white, context.theme)))
        array.recycle()
        super.setOnClickListener {
            checkbox.toggle()
        }
    }

    var onCheckChangedListener: CompoundButton.OnCheckedChangeListener? = null
        set(value) = checkbox.setOnCheckedChangeListener(value)

    val isChecked:Boolean
    get() {
        return checkbox.isChecked
    }

    fun toggle():Boolean {
        checkbox.toggle()
        return checkbox.isChecked
    }
}