package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import com.mobile.Constants
import com.mobile.model.GuestTicket
import com.mobile.utils.expandTouchArea
import com.moviepass.R
import com.mobile.utils.text.toCurrency
import kotlinx.android.synthetic.main.layout_ticket_container.view.*

class TicketContainer(context: Context, attributeSet: AttributeSet? = null) : ConstraintLayout(context, attributeSet) {

    val fee = Constants.CONVENIENCE_FEE

    var constraints: TicketConstraint? = TicketConstraint(max = 4, min = 0)
        set(value) {
            field = value
            setFields()
        }

    private fun setFields() {
        addUpToX.text = resources.getQuantityString(R.plurals.add_up_to_x, constraints?.max
                ?: 0, constraints?.max ?: 0)
        ticketCounter.removeAllViews()
        constraints?.guestTicketTypes?.forEach {
            ticketCounter.addView(TicketCounterView(context).apply {
                bind(it, constraint = constraints)
            })
        }

    }

    private val listener: TicketListener = object : TicketListener {
        override fun onTickets(ticketPurchaseData: List<TicketPurchaseData>) {
            val fee = ticketPurchaseData
                    .sumByDouble {
                        it.tickets * fee
                    }
            val total = ticketPurchaseData
                    .sumByDouble {
                        it.tickets * it.ticket.costAsDollars
                    } + fee
            convenienceFee.text = fee.toCurrency()
            totalTxt.text = resources.getString(R.string.total, total.toCurrency())
        }
    }

    val ticketPurchaseData: List<TicketPurchaseData>
        get() = ticketCounter?.ticketPurchaseData ?: emptyList()

    init {
        inflate(context, R.layout.layout_ticket_container, this)
        ticketCounter.ticketListener = listener
        arrayOf(convenienceFee, infoIcon).forEach {
            it.setOnClickListener {
                showConvenineceFeeDialog()
            }
        }
        sofaIcon.expandTouchArea()
        setFields()
    }

    private fun showConvenineceFeeDialog() {
        AlertDialog.Builder(context).setTitle(
                R.string.convenience_fee_title
        ).setMessage(resources.getString(R.string.convenience_fee_message, fee.toCurrency()))
                .setPositiveButton(android.R.string.ok, null).show()
    }
}