package com.mobile.seats

import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.mobile.widgets.MPAlertDialog
import com.moviepass.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_bring_a_friend.*

class BringAFriendFragment : Fragment(), BringAFriendListener {

    private val payloadSub: BehaviorSubject<SelectSeatPayload> = BehaviorSubject.create()

    private var bringAFriend: Checkin? = null

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
        adapter = BringAFriendPagerAdapter(bringAFriend, UserPreferences.restrictions.userSegments, childFragmentManager)
        viewPager.adapter = adapter
        payloadSub.onNext(SelectSeatPayload(
                checkin = bringAFriend
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
                        checkin = payloadSub.value.checkin,
                        selectedSeats = payloadSub.value?.selectedSeats,
                        ticketPurchaseData = payload
                ))
    }

    override fun onContinueWithoutGuests(payload: List<TicketPurchaseData>) {
        viewPager.currentItem++
        payloadSub.onNext(
                SelectSeatPayload(
                        checkin = bringAFriend,
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
            MPAlertDialog(it)
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
        val checkin = payload.checkin?:return
        val screeningToken = ScreeningToken(
                checkIn = checkin,
                reservation = result,
                confirmationCode = result.eTicketConfirmation,
                seatSelected = payload.selectedSeats?.map { SeatSelected(selectedSeatRow = it.row, selectedSeatColumn = it.column, seatName = it.seatName) }
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
        fun newInstance(bringAFriendPayload: Checkin): BringAFriendFragment {
            return BringAFriendFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("data", bringAFriendPayload)
                }
            }
        }
    }
}