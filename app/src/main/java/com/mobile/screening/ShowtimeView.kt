package com.mobile.screening

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import com.moviepass.R

class ShowtimeView(context: Context) : AppCompatTextView(ContextThemeWrapper(context, R.style.ShowtimeButton)) {

    var time: String? = null

    init {
        val startMargin = resources.getDimension(R.dimen.card_button_margin_start).toInt()
        val margin = resources.getDimension(R.dimen.more_spacing).toInt()
        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)?.apply {
            marginStart = startMargin
            topMargin = margin
            bottomMargin = margin
        }
    }

    fun bind(time: String?, screening: ScreeningPresentation) {
        this.time = time
        this.text = time
        when (screening.selected?.second == time) {
            true -> isSelected = true
            else -> isSelected = false
        }
        isEnabled = screening.enabled
    }
}