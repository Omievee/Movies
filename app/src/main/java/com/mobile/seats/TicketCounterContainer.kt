package com.mobile.seats

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mobile.model.GuestTicket
import com.mobile.model.SeatInfo

class TicketCounterContainer(context: Context, attr: AttributeSet? = null) : LinearLayout(context, attr) {

    val constraint = TicketConstraint(min = 0, max = 4)

    var ticketListener: TicketListener? = null

    init {
        orientation = VERTICAL
    }

    val ticketPurchaseData: List<TicketPurchaseData>
        get() {
            return (0..childCount).map {
                val view = getChildAt(it)
                when (view) {
                    is TicketCounterView -> TicketPurchaseData(view.value, view.ticket
                            ?: GuestTicket())
                    else -> TicketPurchaseData(0, GuestTicket())
                }
            }
        }

    private val listener = object : TicketCounterListener {
        override fun onChange(ticketPurchaseDatas: TicketPurchaseData) {
            val total = (0..childCount).sumBy {
                val view = getChildAt(it)
                when (view) {
                    is TicketCounterView -> view.value
                    else -> 0
                }
            }
            val newMax = when {
                total == constraint.max -> 0
                else -> constraint.max
            }
            (0..childCount).map {
                getChildAt(it) as? TicketCounterView
            }.filter { it is TicketCounterView }
                    .forEach {
                        it?.constraint = TicketConstraint(max = newMax, min = 0)
                    }
            ticketListener?.let {
                it.onTickets(ticketPurchaseData = ticketPurchaseData)
            }
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        if (child is TicketCounterView) {
            child.listener = listener
        }
    }
}

interface TicketListener {
    fun onTickets(ticketPurchaseData: List<TicketPurchaseData>)
}

data class TicketPurchaseData(
        val tickets: Int = 0,
        val ticket: GuestTicket = GuestTicket()) {
    val totalCost: Double
        get() = tickets * ticket.costAsDollars
}