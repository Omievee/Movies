package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.model.GuestTicketType
import com.mobile.model.SeatInfo
import com.mobile.network.RestClient
import com.mobile.responses.SeatingsInfoResponse
import com.moviepass.R
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_seat_selection.*

class SeatSelectionFragment : Fragment() {

    var listener: BringAFriendListener? = null

    val state = State()

    var seatSelectedList: SeatsSelectedListener = object : SeatsSelectedListener {
        override fun onSeatsSelected(seats: Set<SeatInfo>) {
            state.payload?.let {
                it.selectedSeats = seats.toList()
                screeningsHeaderView?.bind(payload = it)
            }
            state.payload?.selectedSeats = seats.toList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_seat_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backButton.setOnClickListener {
            listener?.onBackPressed()
        }
        closeButton.setOnClickListener {
            listener?.onClosePressed()
        }
        continueButton.setOnClickListener {
            var emails = state.payload?.emails ?: return@setOnClickListener
            val size = emails.size
            val childTickets = state.payload?.ticketPurchaseData?.sumBy {
                when (it.ticket.ticketType == GuestTicketType.CHILD_COMPANION) {
                    true -> it.tickets
                    else -> 0
                }
            } ?: 0
            val needed = Math.max(0, state.seatsNeeded - (1 + childTickets))
            when {
                size > needed -> {
                    emails = emails.subList(0, needed)
                }
                size < needed -> {
                    (size until needed).forEach {
                        emails.add(GuestEmail(index = it))
                    }
                }
            }
            val seatsMatch = state.seatsNeeded == state.payload?.selectedSeats?.size ?: 0
            when (seatsMatch) {
                true -> listener?.onSeatSelectionContinue(state.payload)
                else -> showSeatsRequiredError()
            }
        }
        seatsView.seatsSelectedListener = seatSelectedList
        subscribe()
    }

    private fun showSeatsRequiredError() {
        val context = context ?: return
        AlertDialog.Builder(context).setTitle(R.string.choose_seats)
                .setMessage(R.string.choose_seats_error)
                .setPositiveButton(android.R.string.ok, { _, _ -> }).show()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = parentFragment as? BringAFriendListener
    }

    private fun subscribe() {
        state.disposable?.dispose()
        state.disposable = listener?.payload()?.subscribe({ v ->
            state.payload = v
            onTicketPurchaseData()
        }, { e ->
            e.printStackTrace()
        })
    }

    private fun onTicketPurchaseData() {
        state.payload?.let {
            screeningsHeaderView.bind(it)
        }
        val theaterId = state.payload?.theater?.id ?: return
        val tribuneId = state.payload?.theater?.tribuneTheaterId ?: return
        val availability = state.payload?.availability
                ?: return
        val performanceInfo = availability.providerInfo
                ?: return
        if (state.seatsInfo == null) {
            state.seatDisposable = RestClient.getAuthenticated()
                    .getSeats(tribuneId, theaterId.toString(), performanceInfo.performanceId)
                    .map { it ->
                        when (it.seatingInfo?.hasNoSeats) {
                            null, true -> throw ApiError()
                            else -> it
                        }
                    }
                    .subscribe({ seatResponse ->
                        state.seatsInfo = seatResponse
                        state.error = null
                        onSeatInfo()
                    }, ({ error ->
                        state.error = error
                        onError()
                    }))
        } else {
            onSeatInfo()
        }
    }

    private fun onSeatInfo() {
        state.seatsInfo?.seatingInfo?.let {
            seatsView.bind(seatingsInfo = it, seatsNeeded = state.seatsNeeded, selectedSeats = state.payload?.selectedSeats)
        }

    }

    private fun onError() {
        seatsView.error()
    }

    override fun onDestroy() {
        super.onDestroy()
        state.onDestroy()
        listener = null
    }
}

class State(
        var payload: SelectSeatPayload? = null,
        var seatsInfo: SeatingsInfoResponse? = null,
        var disposable: Disposable? = null,
        var seatDisposable: Disposable? = null,
        var error: Throwable? = null
) {

    val seatsNeeded: Int
        get() {
            return payload?.ticketPurchaseData?.sumBy {
                it.tickets
            }?.plus(1) ?: 1
        }

    fun onDestroy() {
        disposable?.dispose()
        seatDisposable?.dispose()
    }
}