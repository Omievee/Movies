package com.mobile.profile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.CompoundButton
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_profile_checkbox.view.*

class ProfileCheckBox(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_profile_checkbox, this)
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun bind(presentation: ProfilePresentation, checkListener: ProfileToggleListener?=null) {
        check.onCheckChangedListener = null
        check.isChecked = presentation.toggled
        check.onCheckChangedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            presentation.toggled = isChecked
            checkListener?.onToggle(presentation)
        }
    }
}