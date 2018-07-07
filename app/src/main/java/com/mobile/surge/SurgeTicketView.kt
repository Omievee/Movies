package com.mobile.surge

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import android.view.View
import com.mobile.model.Availability
import com.mobile.model.GuestTicketType
import com.mobile.model.Surge
import com.mobile.utils.text.toCurrency
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_guest_ticket.view.*

class SurgeTicketView(context: Context, attributeSet: AttributeSet?=null) : ConstraintLayout(context,attributeSet) {

    var infoClickListener:InfoClickListener? = null

    init {
        inflate(context, R.layout.layout_guest_ticket, this)
        infoIcon.visibility = View.VISIBLE
        infoIcon.setOnClickListener {
            infoClickListener?.onClickInfo()
        }
    }

    fun bind(surge:Surge, infoClickListener: InfoClickListener?=null) {
        ticketType.setText(R.string.peak_surcharge)
        this.infoClickListener = infoClickListener
        price.text = surge.costAsDollars
        val one = 1
        quantity.text = one.toString()
        total.text = surge.costAsDollars
    }
}

interface InfoClickListener {
    fun onClickInfo()
}
