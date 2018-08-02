package com.mobile.seats

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.mobile.Constants
import com.mobile.model.GuestTicket
import com.mobile.model.GuestTicketType
import com.moviepass.R

class GuestTicketsContainer(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    fun bind(payload: SelectSeatPayload, freeClickListener: OnClickListener? = null) {
        removeAllViews()
        payload.ticketPurchaseData
                ?.filterNot {
                    it.tickets == 0
                }
                ?.forEach {
                    addView(GuestTicketView(context).apply {
                        bind(it)
                    })
                }
        val tickets = payload.ticketPurchaseData?.sumBy {
            it.tickets
        } ?: 0
        when {
            tickets > 0 -> {
                addView(GuestTicketView(context).apply {
                    bind(TicketPurchaseData(ticket = GuestTicket(GuestTicketType.CONVENIENCE_FEE, Constants.CONVENIENCE_FEE_CENTS),
                            tickets = tickets))
                })
            }
        }
        when (payload.totalGuestTickets>0) {
            false -> {

            }
            true -> {
                addView(View(ContextThemeWrapper(context, R.style.divider), null, R.style.divider), MATCH_PARENT, resources.getDimension(R.dimen.dp_1).toInt())
                addView(TotalView(context).apply {
                    bind(payload)
                    setOnClickListener(freeClickListener)
                }, MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    topMargin = resources.getDimension(R.dimen.margin_standard_and_half).toInt()
                })
            }
        }
    }
}