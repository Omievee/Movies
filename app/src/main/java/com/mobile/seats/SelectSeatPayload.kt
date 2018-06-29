package com.mobile.seats

import com.mobile.Constants
import com.mobile.model.Screening
import com.mobile.model.SeatInfo
import com.mobile.model.Theater2

class SelectSeatPayload(
        val theater: Theater2? = null,
        val screening: Screening? = null,
        val showtime: String? = null,
        val ticketPurchaseData: List<TicketPurchaseData>? = null,
        var selectedSeats: List<SeatInfo>? = null,
        var emails: MutableList<GuestEmail> = mutableListOf()
) {

    val fee: Double
        get() {
            return ticketPurchaseData
                    ?.sumByDouble {
                        it.tickets * Constants.CONVENIENCE_FEE
                    } ?: 0.0
        }

    val total: Double
        get() {
            return ticketPurchaseData
                    ?.sumByDouble {
                        it.tickets * it.ticket.costAsDollars
                    }?.plus(fee) ?: fee
        }

    val totalGuestTickets: Int
        get() {
            return ticketPurchaseData
                    ?.sumBy {
                        it.tickets
                    } ?: 0
        }
}

data class GuestEmail(
        var email: String? = null,
        var status: EmailStatus? = null,
        var index: Int? = null
)

enum class EmailStatus {
    APPROVED,
    EXISTING
}