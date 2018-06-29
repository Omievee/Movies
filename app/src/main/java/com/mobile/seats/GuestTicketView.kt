package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.TextViewCompat
import android.util.AttributeSet
import com.mobile.model.GuestTicketType
import com.mobile.utils.text.toCurrency
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_guest_ticket.view.*

class GuestTicketView(context: Context?, attributeSet: AttributeSet?=null) : ConstraintLayout(context,attributeSet) {

    init {
        inflate(context, R.layout.layout_guest_ticket, this)
    }

    fun bind(payload: TicketPurchaseData) {
        when (payload.ticket.ticketType) {
            GuestTicketType.CONVENIENCE_FEE -> {
                ticketType.text = resources.getString(R.string.convenience_fee)
                TextViewCompat.setTextAppearance(ticketType, R.style.MPText_Italic)
            }
            else -> {
                ticketType.text = resources.getString(R.string.tickets, payload.ticket.ticketType?.display)
            }
        }
        price.text = payload.ticket.costAsDollars.toCurrency()
        quantity.text = payload.tickets.toString()
        total.text = payload.totalCost.toCurrency()
    }
}
