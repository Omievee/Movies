package com.mobile.seats

import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.model.*
import com.mobile.reservation.Checkin

class SelectSeatPayload(
        val checkin: Checkin? = null,
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
            val discount = if (checkin?.softCap == true) {
                UserPreferences.restrictions.cappedPlan?.asDollars ?: 0.0
            } else {
                0.0
            }
            val softCapTicketPrice = if(checkin?.softCap==true) {
                checkin.availability.guestsTicketTypes?.firstOrNull { it.ticketType==GuestTicketType.ADULT_COMPANION }?.costAsDollars?:0.0
            } else {
                0.0
            }
            return (ticketPurchaseData
                    ?.sumByDouble {
                        it.tickets * it.ticket.costAsDollars
                    }?.plus(fee) ?: fee).minus(discount).plus(softCapTicketPrice)
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