package com.mobile.seats

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.BackFragment
import com.mobile.UserPreferences
import com.mobile.model.ScreeningToken
import com.mobile.model.SeatPosition
import com.mobile.model.SeatSelected
import com.mobile.reservation.Checkin
import com.mobile.reservation.ReservationActivity
import com.mobile.responses.ReservationResponse
import com.mobile.rx.Schedulers
import com.moviepass.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_bring_a_friend.*
import org.parceler.Parcels

class BringAFriendFragment : Fragment(), BringAFriendListener {

    private val payloadSub: BehaviorSubject<SelectSeatPayload> = BehaviorSubject.create()

    private var bringAFriend: BringAFriendPayload? = null

    private var adapter: BringAFriendPagerAdapter? = null

    override fun payload(): Observable<SelectSeatPayload> {
        return payloadSub.compose(Schedulers.observableDefault())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bring_a_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bringAFriend = arguments?.getParcelable("data")
        viewPager.offscreenPageLimit = 5
        val screening = bringAFriend?.screening ?: return
        val availability = bringAFriend?.availability ?: return
        adapter = BringAFriendPagerAdapter(screening, availability, UserPreferences.restrictions.userSegments, childFragmentManager)
        viewPager.adapter = adapter
        payloadSub.onNext(SelectSeatPayload(
                theater = bringAFriend?.theater,
                screening = bringAFriend?.screening,
                availability = bringAFriend?.availability
        ))
    }

    override fun navigateToSeats(unavailableSeats: List<SeatPosition>) {
        adapter?.seatIndex?.let {
            viewPager.currentItem = it
        }
    }

    override fun onGuestsContinue(payload: List<TicketPurchaseData>) {
        viewPager.currentItem++
        payloadSub.onNext(
                SelectSeatPayload(
                        screening = bringAFriend?.screening,
                        theater = bringAFriend?.theater,
                        availability = bringAFriend?.availability,
                        selectedSeats = payloadSub.value?.selectedSeats,
                        ticketPurchaseData = payload
                ))
    }

    override fun onContinueWithoutGuests(payload: List<TicketPurchaseData>) {
        viewPager.currentItem++
        payloadSub.onNext(
                SelectSeatPayload(
                        screening = bringAFriend?.screening,
                        theater = bringAFriend?.theater,
                        availability = bringAFriend?.availability,
                        selectedSeats = emptyList(),
                        ticketPurchaseData = payload
                ))
    }

    override fun onEmailContinueClicked(payload: SelectSeatPayload?) {
        val pay = payload ?: return
        viewPager.currentItem++
        payloadSub.onNext(pay)
    }

    override fun onSeatSelectionContinue(payload: SelectSeatPayload?) {
        val pay = payload ?: return
        var inc = 1;
        if (pay.emails.size == 0) {
            inc++
        }
        viewPager.currentItem += inc
        payloadSub.onNext(pay)
    }

    override fun onClosePressed() {
        context?.let {
            AlertDialog.Builder(it)
                    .setMessage(R.string.are_you_sure_exit)
                    .setPositiveButton(R.string.exit_checkout, { _, _ ->
                        activity?.finish()
                    })
                    .setNegativeButton(R.string.stay, { _, _ ->

                    }).show()
        }

    }

    override fun onTicketsPurchased(result: ReservationResponse) {
        val payload: SelectSeatPayload = payloadSub.value ?: return
        val activity = activity ?: return
        val screening = payload.screening ?: return
        val theater = payload.theater ?: return
        val availability = payload.availability ?: return
        val screeningToken = ScreeningToken(
                Checkin(screening = screening,
                        availability = availability,
                        theater = theater),
                reservation = result,
                confirmationCode = result.eTicketConfirmation,
                seatSelected = payload.selectedSeats?.map { SeatSelected(it.row, it.column, it.seatName) }
        )
        activity.finish()
        startActivity(ReservationActivity.newInstance(activity, screeningToken))
    }

    override fun onBackPressed() {
        adapter?.currentItem?.let {
            if (it is BackFragment) {
                if (it.onBack()) {
                    return
                }
            }
        }

        val emailIndex = adapter?.emailIndex ?: Integer.MIN_VALUE
        when (viewPager.currentItem) {
            0 -> activity?.finish()
            emailIndex + 1 -> {
                val payload = payloadSub.value
                viewPager.currentItem = when (payload.emails.isEmpty()) {
                    true -> emailIndex - 1
                    false -> emailIndex
                }
            }
            else -> viewPager.currentItem--
        }
    }

    companion object {
        fun newInstance(bringAFriendPayload: BringAFriendPayload): BringAFriendFragment {
            return BringAFriendFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("data", bringAFriendPayload)
                }
            }
        }
    }
}