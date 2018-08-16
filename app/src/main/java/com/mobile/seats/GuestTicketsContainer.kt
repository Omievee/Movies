package com.mobile.seats

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.model.GuestTicket
import com.mobile.model.GuestTicketType
import com.moviepass.R

class GuestTicketsContainer(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    fun bind(payload: SelectSeatPayload, freeClickListener: OnClickListener? = null) {
        removeAllViews()
        val tickets = payload.ticketPurchaseData?.sumBy {
            it.tickets
        } ?: 0
        if (tickets > 0) {
            addView(GuestTicketView(context).apply {
                bind(payload, TicketPurchaseData(
                        ticket = GuestTicket(GuestTicketType.GUEST_TICKETS)
                ))
            })
        }
        payload.ticketPurchaseData
                ?.filterNot {
                    it.tickets == 0
                }
                ?.forEach {
                    addView(GuestTicketView(context).apply {
                        bind(payload, it)
                    })
                }
        when {
            tickets > 0 -> {
                addView(GuestTicketView(context).apply {
                    bind(payload, TicketPurchaseData(ticket = GuestTicket(GuestTicketType.CONVENIENCE_FEE, Constants.CONVENIENCE_FEE_CENTS),
                            tickets = tickets))
                })
            }
        }
        if (payload.checkin?.softCap == true) {
            addView(GuestTicketView(context).apply {
                bind(payload, TicketPurchaseData(
                        ticket = GuestTicket(GuestTicketType.YOUR_TICKET)
                ))})
            if(payload.checkin.hasAdultTicketPrice) {
                addView(GuestTicketView(context).apply {
                    bind(payload, TicketPurchaseData(
                            tickets = 1, ticket = GuestTicket(ticketType = GuestTicketType.SOFT_CAP_TICKET, price = payload.checkin.availability.guestsTicketTypes?.firstOrNull {
                        it.ticketType == GuestTicketType.ADULT_COMPANION
                    }?.price ?: 0)
                    ))
                })
            }

            addView(GuestTicketView(context).apply {
                bind(payload, TicketPurchaseData(
                        tickets = 1, ticket = GuestTicket(ticketType = GuestTicketType.MOVIEPASS_DISCOUNT, price = -(UserPreferences.restrictions.cappedPlan?.discount
                        ?: 0))
                ))
            })

        }
        when (payload.totalGuestTickets > 0 || payload.checkin?.softCap == true) {

            false -> {

            }
            true -> {
                addView(View(ContextThemeWrapper(context, R.style.divider), null, R.style.divider), MarginLayoutParams(MATCH_PARENT, resources.getDimension(R.dimen.dp_1).toInt()).apply {
                    topMargin = resources.getDimension(R.dimen.margin_standard).toInt()
                    leftMargin = topMargin
                    rightMargin = topMargin
                })
                addView(TotalView(context).apply {
                    bind(payload)
                    setOnClickListener(freeClickListener)
                }, MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    topMargin = resources.getDimension(R.dimen.margin_standard).toInt()
                })
            }
        }
    }
}