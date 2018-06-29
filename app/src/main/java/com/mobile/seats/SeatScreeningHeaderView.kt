package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_screening_header.view.*

class SeatScreeningHeaderView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.layout_screening_header, this)
    }

    fun bind(payload: SelectSeatPayload) {
        movieName.text = payload.screening?.title
        theaterName.text = payload.theater?.name
        showtime.text = payload.showtime
        val totalTicketsNeeded =
                payload.ticketPurchaseData?.sumBy {
                    it.tickets
                }?.plus(1) ?: 1
        selectSeats.text = resources.getQuantityString(
                R.plurals.select_seats,
                totalTicketsNeeded,
                totalTicketsNeeded

        )
        seats.text = payload.selectedSeats?.sortedWith(compareBy({ it.row }, { it.column }))?.joinToString(", ") {
            it.seatName ?: ""
        }
        seatsHeader.text = resources.getQuantityString(R.plurals.seats, payload.ticketPurchaseData?.size
                ?: 1, payload.ticketPurchaseData?.size ?: 1)
    }

}