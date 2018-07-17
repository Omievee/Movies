package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.model.GuestTicket
import com.mobile.model.ProviderInfo
import com.mobile.model.TicketType
import com.mobile.network.Api
import com.mobile.network.RestClient
import com.mobile.requests.TicketInfoRequest
import com.mobile.reservation.Checkin
import com.mobile.utils.showBottomFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_confirm_details.*
import javax.inject.Inject

class ConfirmDetailsFragment : Fragment() {

    @Inject
    lateinit var locationManager: com.mobile.location.LocationManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var api: Api

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

    private fun subscribe() {
        disposable?.dispose()
        disposable = listener?.payload()
                ?.subscribe({
                    payload = it
                    guestTicketsKey.visibility = when (it.totalGuestTickets) {
                        0 -> View.GONE
                        else -> View.VISIBLE
                    }
                    billingCardOnfile.visibility = guestTicketsKey.visibility
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
                R.string.e_ticket_cancellation_policy_modal_title
        )
                .setMessage(R.string.e_ticket_cancellation_policy_modal)
                .setPositiveButton(R.string.continue_button) { _, _ ->
                    reserveTickets()
                }.setNegativeButton(R.string.cancel, null).show()

    }

    private fun reserveTickets() {
        val local = locationManager.lastLocation()
        val payload = payload ?: return
        val availability = payload.availability ?: return
        val screening = payload.screening ?: return
        val theater = payload.theater ?: return
        val tpd = payload.ticketPurchaseData ?: emptyList()
        val provideInfo = availability.providerInfo ?: return
        val lat: Double
        val lng: Double
        when (local == null) {
            true -> {
                val location = UserPreferences.location
                lat = location.latitude
                lng = location.longitude
            }
            false -> {
                val loc = local ?: return
                lat = loc.lat
                lng = loc.lon
            }
        }

        val mySeat = payload.selectedSeats?.first()
        if (availability.ticketType == TicketType.SELECT_SEATING) {
            if (mySeat == null) return
        }


        val emails = payload.emails
        val hasMatchingSeatCount = mySeat == null || ((payload.selectedSeats?.size
                ?: 0) - 1) == tpd.sumBy { it.tickets }
        when (hasMatchingSeatCount) {
            false -> return
        }
        val guestTickets = tpd.filter { it.tickets > 0 }.map { ticketPurchaseData ->
            val expanded = (0 until ticketPurchaseData.tickets).map {
                ticketPurchaseData
            }
            expanded
        }.flatMap { it ->
            it.mapIndexed { index, tpd ->
                val seat = payload.selectedSeats?.get(index + 1)
                GuestTicket(ticketType = tpd.ticket.ticketType,
                        price = tpd.ticket.price,
                        seatPosition = seat?.asPosition(),
                        email = when {
                            index < emails.size -> emails[index].email
                            else -> null
                        }
                )
            }
        }
        getTickets.progress = true
        getTicketsDisposable?.dispose()
        val checkIn = Checkin(
                screening = screening,
                theater = theater,
                availability = availability)
        getTicketsDisposable = RestClient
                .getAuthenticated()
                .reserve(
                        TicketInfoRequest(
                                performanceInfo = ProviderInfo(
                                        tribuneTheaterId = payload.theater?.tribuneTheaterId ?: 0,
                                        normalizedMovieId = provideInfo.normalizedMovieId,
                                        externalMovieId = provideInfo.externalMovieId,
                                        format = provideInfo.format,
                                        performanceId = provideInfo.performanceId,
                                        dateTime = provideInfo.dateTime,
                                        seatPosition = mySeat?.asPosition(),
                                        guestsAllowed = payload.screening?.maximumGuests,
                                        guestTickets = when (guestTickets.isEmpty()) {
                                            true -> null
                                            false -> guestTickets
                                        }
                                ),
                                longitude = lat,
                                latitude = lng
                        )
                )
                .doAfterTerminate { getTickets.progress = false }
                .subscribe({ result ->
                    result?.let {
                        listener?.onTicketsPurchased(result)
                        analyticsManager.onCheckinSuccessful(checkIn = checkIn,
                                reservationResponse = it
                        )
                    }
                }
                ) { error ->
                    if (error is ApiError) {
                        val context = context ?: return@subscribe
                        AlertDialog.Builder(context).setTitle(error.error.title)
                                .setMessage(error.error.message)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    when (error.httpErrorCode) {
                                        409 -> {
                                            listener?.navigateToSeats(error.error.unavailablePositions
                                                    ?: emptyList())
                                        }
                                        else -> {
                                        }
                                    }
                                }
                                .show()
                    }
                    analyticsManager.onCheckinFailed(checkIn)
                }
    }
}