package com.mobile.drawable

import android.content.res.Resources
import android.graphics.drawable.StateListDrawable
import android.support.v4.content.res.ResourcesCompat.getDrawable
import android.util.StateSet

class StateDrawableBuilder(
        val normal: Int? = null,
        val selected: Int? = null,
        val disabled: Int? = null,
        val resources:Resources
) {
    fun build():StateListDrawable {
        val s = StateListDrawable()
        selected?.let {
            s.addState(intArrayOf(android.R.attr.state_selected), getDrawable(resources, it, null))
            s.addState(intArrayOf(android.R.attr.state_checked), getDrawable(resources, it, null))
        }
        disabled?.let {
            s.addState(intArrayOf(-android.R.attr.state_enabled), getDrawable(resources, it, null))
        }
        normal?.let {
            selected?.let {
                s.addState(intArrayOf(android.R.attr.state_pressed), getDrawable(resources, it, null)?.mutate()?.apply {
                    alpha=85
                })
            }
            s.addState(StateSet.WILD_CARD, getDrawable(resources, it, null));
        }
        return s;
    }
}