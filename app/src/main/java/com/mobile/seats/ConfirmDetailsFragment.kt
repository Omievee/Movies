package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.TextViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.model.GuestTicket
import com.mobile.model.GuestTicketType
import com.mobile.model.ProviderInfo
import com.mobile.model.TicketType
import com.mobile.network.Api
import com.mobile.network.RestClient
import com.mobile.requests.TicketInfoRequest
import com.mobile.utils.showBottomFragment
import com.mobile.widgets.MPAlertDialog
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
                    billingCardOnfile.visibility = when (it.totalGuestTickets > 0 || it.checkin?.softCap == true) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }
                    billingCardOnfile.text = when {
                        UserPreferences.restrictions.cappedPlan?.remaining==0-> getString(R.string.billing_card_on_file)
                        else -> getString(R.string.billing_card_on_file_abbreviated)
                    }

                    cancellationPolicy.text = when (it.checkin?.availability?.ticketType == TicketType.STANDARD) {
                        true -> {
                            TextViewCompat.setTextAppearance(cancellationPolicy, R.style.CancellationPolicy)
                            getString(R.string.cancellation_and_refund_policy)
                        }
                        else -> {
                            TextViewCompat.setTextAppearance(cancellationPolicy, R.style.ETicketCanNotBeCancelled)
                            getString(R.string.e_ticket_can_not_be_cancelled)

                        }
                    }
                    cancellationPolicy.setOnClickListener {v->
                        when (it.checkin?.availability?.ticketType == TicketType.STANDARD) {
                            true -> {
                                showBottomFragment(SheetData(
                                        title = getString(R.string.cancellation_and_refund_policy),
                                        description = getString(R.string.cancellation_softcap),
                                        subDescription = getString(R.string.cancellation_no_refunds_once_redeemed)
                                ))
                            }
                            false -> {
                            }
                        }
                    }
                    moviePosterHeader.bind(it)
                    guestTicketContainer.bind(it, freeClickListener)

                    getTickets.setOnClickListener {
                        val checkin = payload?.checkin ?: return@setOnClickListener
                        when (checkin.softCap == true) {
                            true -> showSoftCapDialog()
                            else -> showDialogToReserveTickets()
                        }
                    }
                }, {
                })
    }

    private fun showSoftCapDialog() {
        val context = activity ?: return
        val checkin = payload?.checkin ?: return
        MPAlertDialog(context)
                .setTitle(R.string.agree_to_payment)
                .setMessage(if (checkin.availability.isETicket()) {
                    R.string.agree_to_payment_e_ticket_description
                } else {
                    R.string.agree_to_payment_description
                })
                .setPositiveButton(R.string.i_agree) { _, _ ->
                    reserveTickets()
                }
                .setNegativeButton(R.string.go_back, null).show()
    }

    private fun showDialogToReserveTickets() {
        val context = context ?: return
       MPAlertDialog(context).setTitle(
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
        val checkin = payload.checkin ?: return
        val tpd = payload.ticketPurchaseData ?: emptyList()
        val provideInfo = checkin.availability.providerInfo ?: return
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
        val seatsIter = payload.selectedSeats?.iterator()
        val mySeat = seatsIter?.next()
        if (checkin.availability.ticketType == TicketType.SELECT_SEATING) {
            if (mySeat == null) return
        }


        val emails = payload.emails.iterator()
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
                val seat = seatsIter?.next()
                GuestTicket(ticketType = tpd.ticket.ticketType,
                        price = tpd.ticket.price,
                        seatPosition = seat?.asPosition(),
                        email = when {
                            tpd.ticket.ticketType == GuestTicketType.CHILD_COMPANION -> null
                            emails.hasNext() -> emails.next().email
                            else -> null
                        }
                )
            }
        }
        getTickets.progress = true
        getTicketsDisposable?.dispose()
        val checkIn = checkin
        val ticketInfoRequest = TicketInfoRequest(
                performanceInfo = ProviderInfo(
                        tribuneTheaterId = payload.checkin.theater.tribuneTheaterId
                                                ?: 0,
                        normalizedMovieId = provideInfo.normalizedMovieId,
                        externalMovieId = provideInfo.externalMovieId,
                        format = provideInfo.format,
                        performanceId = provideInfo.performanceId,
                        dateTime = provideInfo.dateTime,
                        seatPosition = mySeat?.asPosition(),
                        guestsAllowed = payload.checkin.screening.maximumGuests,
                        guestTickets = when (guestTickets.isEmpty()) {
                            true -> null
                            false -> guestTickets
                        }
                ),
                longitude = lng,
                latitude = lat
        )
        getTicketsDisposable = RestClient
                .getAuthenticated()
                .reserve(
                        ticketInfoRequest
                )
                .doOnSubscribe {
                    analyticsManager.onCheckinAttempt(checkIn)
                }
                .doAfterTerminate { getTickets.progress = false }
                .subscribe({ result ->
                    result?.let {
                        listener?.onTicketsPurchased(result)
                        analyticsManager.onCheckinSuccessful(checkIn = checkIn,
                                reservationResponse = it
                        )
                        analyticsManager.onTicketsPurchased(ticketInfoRequest)
                    }
                }
                ) { error ->
                    analyticsManager.onCheckinFailed(checkIn)
                    if (error is ApiError) {
                        val context = context ?: return@subscribe
                        MPAlertDialog(context).setTitle(error.error.title)
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