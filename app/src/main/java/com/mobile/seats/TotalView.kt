package com.mobile.seats

import android.app.AlertDialog
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
import com.mobile.utils.padding
import com.mobile.utils.text.toCurrency
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_total.view.*

class TotalView(context: Context, attr: AttributeSet? = null) : ConstraintLayout(context, attr) {

    init {
        inflate(context, R.layout.layout_total, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
        infoIcon.setOnClickListener {
            AlertDialog.Builder(context).setTitle(R.string.discounted_ticket_price)
                    .setMessage(R.string.discounted_ticket_price_descriptiopn)
                    .setPositiveButton(R.string.ok,null).show()
        }
    }

    fun bind(payload: SelectSeatPayload) {
        val total = payload.total
        val totalSpan = SpannableStringBuilder().apply {
            val span = SpannableString("Total").apply {
                setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.white_ish, null)), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(span)
            append(":   ")
            val str = if (payload.checkin?.softCap == true && !payload.checkin.hasAdultTicketPrice) {
                infoIcon.visibility = View.GONE
                "- -"
            } else {
                infoIcon.visibility = View.GONE
                total.toCurrency()
            }
            linkText.visibility = when {
                payload.checkin?.softCap==true || payload.totalGuestTickets>0 -> View.VISIBLE
                else -> View.GONE
            }
            val totalVal = SpannableString(str).apply {
                setSpan(TextAppearanceSpan(context, R.style.MPText_Bold), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(totalVal)
        }
        if(payload.checkin?.hasAdultTicketPrice==true) {
            totalTxt.text = totalSpan
        } else {
            totalTxt.text = ""
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        linkText.setOnClickListener(l)
    }

}
