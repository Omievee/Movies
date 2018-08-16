package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.mobile.model.GuestTicket
import com.mobile.model.GuestTicketType
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_ticket_counter.view.*
import com.mobile.utils.text.toCurrency

class TicketCounterView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var ticket: GuestTicket? = null
    var constraint: TicketConstraint? = null
        set(value) {
            field = value
            setFields()
        }

    private fun setFields() {
        val tpd = constraint?.ticketPurchaseData?.find {
            it.ticket == ticket
        }
        value = tpd?.tickets ?: value
        add.isEnabled = value < constraint?.max ?: 0
        subtract.isEnabled = value > constraint?.min ?: 0
        total.text = ((ticket?.costAsDollars ?: 0.0) * value).toCurrency()
        amount.text = "${value}"
    }

    var listener: TicketCounterListener? = null
    var value: Int = 0

    init {
        inflate(context, R.layout.layout_ticket_counter, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, resources.getDimension(R.dimen.button_height).toInt())
        add.setOnClickListener {
            value++
            ticket?.let {
                listener?.onChange(TicketPurchaseData(value, it))
            }

            bind(ticket, constraint, listener)
        }
        subtract.setOnClickListener {
            value--
            ticket?.let {
                listener?.onChange(TicketPurchaseData(value, it))
            }
            bind(ticket, constraint, listener)
        }
    }

    fun bind(ticket: GuestTicket?, constraint: TicketConstraint?, ticketCounterListener: TicketCounterListener? = null) {
        this.ticket = ticket
        this.constraint = constraint
        this.listener = ticketCounterListener
        label.text = resources.getString(R.string.tickets, ticket?.ticketType?.display)
        setFields()
    }

}

interface TicketCounterListener {
    fun onChange(ticketPurchaseData: TicketPurchaseData)
}

data class TicketConstraint(
        val min: Int = 0,
        val max: Int = 4,
        val convenienceFee: Double = 1.50,
        val guestTicketTypes: List<GuestTicket>? = null,
        val ticketPurchaseData: List<TicketPurchaseData>? = null
)