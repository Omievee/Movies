package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.utils.text.toCurrency
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_total.view.*

class TotalView(context: Context, attr: AttributeSet? = null) : ConstraintLayout(context, attr) {

    init {
        inflate(context, R.layout.layout_total, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun bind(payload: SelectSeatPayload) {
        val total = payload.total
        totalTxt.text = SpannableStringBuilder(resources.getString(R.string.total, total.toCurrency())).apply {
            setSpan(ForegroundColorSpan(resources.getColor(R.color.white_ish)), 0, length - total.toCurrency().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(resources.getColor(R.color.red)), length - total.toCurrency().length, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(TextAppearanceSpan(context, R.style.MPText_Bold), length - total.toCurrency().length, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        freeGuestPolicy.setOnClickListener(l)
    }

}
