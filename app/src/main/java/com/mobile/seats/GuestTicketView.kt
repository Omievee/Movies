package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentManager
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import com.mobile.model.GuestTicketType
import com.mobile.model.TicketType
import com.mobile.utils.padding
import com.mobile.utils.text.toCurrency
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_guest_ticket.view.*

class GuestTicketView(context: Context?, attributeSet: AttributeSet? = null) : ConstraintLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.layout_guest_ticket, this)
        padding(top = resources.getDimension(R.dimen.margin_half).toInt(), bottom = resources.getDimension(R.dimen.margin_half).toInt())
    }

    fun bind(seatPayload: SelectSeatPayload, payload: TicketPurchaseData) {
        ticketType.text = when (payload.ticket.ticketType) {
            GuestTicketType.CONVENIENCE_FEE -> resources.getString(R.string.convenience_fee)
            GuestTicketType.MOVIEPASS_DISCOUNT,
            GuestTicketType.YOUR_TICKET,
            GuestTicketType.SOFT_CAP_TICKET -> when (payload.tickets == 0) {
                false -> payload.ticket.ticketType.display + ":"
                true -> GuestTicketType.YOUR_TICKET.display + ":"
            }
            else -> resources.getString(R.string.tickets, payload.ticket.ticketType?.display)
        }
        price.text = when (payload.ticket.ticketType) {
            GuestTicketType.GUEST_TICKETS, GuestTicketType.YOUR_TICKET, GuestTicketType.SOFT_CAP_TICKET, GuestTicketType.MOVIEPASS_DISCOUNT -> ""
            else -> payload.ticket.costAsDollars.toCurrency()
        }
        quantity.text = when (payload.ticket.ticketType) {
            GuestTicketType.GUEST_TICKETS, GuestTicketType.YOUR_TICKET, GuestTicketType.SOFT_CAP_TICKET, GuestTicketType.MOVIEPASS_DISCOUNT -> ""
            else -> payload.tickets.toString()
        }
        total.text = when (payload.ticket.ticketType) {
            GuestTicketType.GUEST_TICKETS, GuestTicketType.YOUR_TICKET -> ""
            else -> payload.totalCost.toCurrency()
        }
        xx.visibility = when (payload.ticket.ticketType) {
            GuestTicketType.MOVIEPASS_DISCOUNT, GuestTicketType.YOUR_TICKET, GuestTicketType.SOFT_CAP_TICKET, GuestTicketType.GUEST_TICKETS -> View.INVISIBLE
            else -> View.VISIBLE
        }
        arrayOf(infoIcon2, description).forEach {
            it.visibility = when {
                payload.ticket.ticketType == GuestTicketType.YOUR_TICKET && seatPayload.totalGuestTickets==0 -> View.VISIBLE
                else -> View.GONE
            }
        }
        infoIcon2.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle(R.string.discounted_ticket_price)
                    .setMessage(R.string.discounted_ticket_price_descriptiopn)
                    .setPositiveButton(R.string.ok, null).show()
        }
        when (payload.ticket.ticketType) {
            GuestTicketType.MOVIEPASS_DISCOUNT -> {
                TextViewCompat.setTextAppearance(ticketType, R.style.MPText_Italic)
                TextViewCompat.setTextAppearance(total, R.style.MPText_Bold)
            }
            GuestTicketType.GUEST_TICKETS -> {
                TextViewCompat.setTextAppearance(ticketType, R.style.MPText_Bold)
            }
            else -> {
                TextViewCompat.setTextAppearance(ticketType, R.style.MPText)
                TextViewCompat.setTextAppearance(total, R.style.MPText)
            }
        }
        when(payload.ticket.ticketType==GuestTicketType.YOUR_TICKET && seatPayload.totalGuestTickets>0) {
            true-> TextViewCompat.setTextAppearance(ticketType, R.style.MPText_Bold)
        }
    }
}
