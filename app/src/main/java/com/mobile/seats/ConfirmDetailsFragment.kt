package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.model.GuestTicket
import com.mobile.model.PerformanceInfoV2
import com.mobile.network.RestClient
import com.mobile.requests.TicketInfoRequest
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_confirm_details.*
import javax.inject.Inject

class ConfirmDetailsFragment : Fragment() {

    @Inject
    lateinit var locationManager: com.mobile.location.LocationManager

    var listener: BringAFriendListener? = null

    var disposable: Disposable? = null
    var getTicketsDisposable: Disposable? = null
    var payload: SelectSeatPayload? = null

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        listener = parentFragment as? BringAFriendListener
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        getTicketsDisposable?.dispose()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_confirm_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton
                .setOnClickListener {
                    listener?.onClosePressed()
                }

        backButton
                .setOnClickListener {
                    listener?.onBackPressed()
                }

        subscribe()
    }

    val freeClickListener = View.OnClickListener {
        showBottomFragment(SheetData(
                error = getString(R.string.free_guest_convenience),
                title = getString(R.string.free_guest_policy),
                description = getString(R.string.free_guest_policy_description)
        ))
    }

    private fun showBottomFragment(sheetData: SheetData) {
        MPBottomSheetFragment.newInstance(sheetData).show(fragmentManager, "")
    }

    private fun subscribe() {
        disposable?.dispose()
        disposable = listener?.payload()
                ?.subscribe({
                    payload = it
                    guestTicketsKey.visibility = when (it.totalGuestTickets) {
                        0 -> View.GONE
                        else -> View.VISIBLE
                    }
                    moviePosterHeader.bind(it)
                    guestTicketContainer.bind(it, freeClickListener)
                    getTickets.setOnClickListener {
                        showDialogToReserveTickets()
                    }
                }, {
                })
    }

    private fun showDialogToReserveTickets() {
        val context = context ?: return
        AlertDialog.Builder(context).setTitle(
                R.string.e_ticket_cancellation_policy
        )
                .setMessage(R.string.e_ticket_cancellation_policy_description)
                .setPositiveButton(R.string.continue_button, { _, _ ->
                    reserveTickets()
                }).setNegativeButton(R.string.cancel, null).show()

    }

    private fun reserveTickets() {
        var local = locationManager.lastLocation()
        val payload = payload ?: return
        val availability = payload.screening?.getAvailability(payload.showtime) ?: return
        val tpd = payload.ticketPurchaseData ?: emptyList()
        val provideInfo = availability.providerInfo ?: return
        val lat = local!!.lat
        val lng = local.lon
        val mySeat = payload.selectedSeats?.first() ?: return
        val emails = payload.emails
        val hasMatchingSeatCount = payload.selectedSeats?.size ?: 0 - 1 == tpd.sumBy { it.tickets }.plus(1)
        when (hasMatchingSeatCount) {
            false -> return
        }
        val guestTickets = tpd.filter { it.tickets > 0 }.mapIndexed { index, ticketPurchaseData ->
            val seat = payload.selectedSeats?.get(index + 1)
            GuestTicket(ticketType = ticketPurchaseData.ticket.ticketType,
                    price = ticketPurchaseData.ticket.price,
                    seatPosition = seat?.asPosition(),
                    email = when {
                        index < emails.size -> emails[index].email
                        else -> null
                    }
            )
        }
        getTickets.progress = true
        getTicketsDisposable?.dispose()
        getTicketsDisposable = RestClient
                .getAuthenticated()
                .reserve(TicketInfoRequest(
                        performanceInfo = PerformanceInfoV2(
                                tribuneTheaterId = payload?.theater?.tribuneTheaterId ?: 0,
                                normalizedMovieId = provideInfo.normalizedMovieId,
                                externalMovieId = provideInfo.externalMovieId,
                                format = provideInfo.format,
                                performanceId = provideInfo.performanceId,
                                dateTime = provideInfo.dateTime,
                                seatPosition = mySeat.asPosition(),
                                guestsAllowed = payload.screening.maximumGuests,
                                guestTickets = guestTickets
                        ),
                        longitude = lng,
                        latitude = lat
                ))
                .doAfterTerminate { getTickets.progress = false }
                .subscribe({ result ->
                    result?.let {
                        listener?.onTicketsPurchased(result)
                    }
                }, { error ->
                    if (error is ApiError) {
                        val context = context ?: return@subscribe
                        AlertDialog.Builder(context).setTitle(error.error?.title)
                                .setMessage(error.error?.message)
                                .setPositiveButton(android.R.string.ok, { _, _ ->
                                    when (error.httpErrorCode) {
                                        409 -> {
                                            listener?.navigateToSeats(error.error?.unavailablePositions
                                                    ?: emptyList())
                                        }
                                        else -> {
                                        }
                                    }
                                })
                                .show()
                    }
                }
                )
    }
}