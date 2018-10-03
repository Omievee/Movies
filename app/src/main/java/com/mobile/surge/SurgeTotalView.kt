package com.mobile.surge

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.mobile.UserPreferences
import com.mobile.reservation.Checkin
import com.mobile.responses.UserInfoResponse
import com.mobile.utils.text.toCurrency
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_total.view.*

class TotalView(context: Context, attr: AttributeSet? = null) : ConstraintLayout(context, attr) {

    init {
        inflate(context, R.layout.layout_total, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun bind(payload: Checkin, userInfo: UserInfoResponse? = null) {
        val surge = payload.screening.getSurge(payload.availability.startTime, UserPreferences.restrictions.userSegments)
        val zero = 0.0
        val total = when(payload.peakPass) {
            null -> surge.costAsDollars
            else -> zero.toCurrency()
        }
        val totalSpan = SpannableStringBuilder().apply {
            val span = SpannableString("Total").apply {
                setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.white_ish, null)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(span)
            append(":   ")
            val totalVal = SpannableString(total).apply {
                setSpan(TextAppearanceSpan(context, R.style.MPText_Bold), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(totalVal)
        }
        totalTxt.text = totalSpan
        linkText.setText(when (userInfo?.billingCard) {
            null -> R.string.add_payment_method
            else -> R.string.edit_payment_method
        })
        linkText.visibility = View.GONE

    }

    override fun setOnClickListener(l: OnClickListener?) {
        linkText.setOnClickListener(l)
    }

}
