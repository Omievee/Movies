package com.mobile.seats

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.*
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TextAppearanceSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moviepass.R
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_add_guests.*
import kotlinx.android.synthetic.main.layout_ticket_container.*

class AddGuestsFragment : Fragment(), SeatPreviewListener, BackFragment {

    var listener: BringAFriendListener? = null
    var disposable: Disposable? = null
    var selectSeatPayload:SelectSeatPayload? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_guests, container, false)
    }

    override fun onBack(): Boolean {
        childFragmentManager.findFragmentById(R.id.seatPreviewOntainer)?.let {
            onClose()
            return true
        }
        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = parentFragment as? BringAFriendListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eTicketCancellationPolicy
                .setOnClickListener {
                    showBottomFragment(SheetData(
                            title = getString(R.string.e_ticket_cancellation_policy),
                            description = getString(R.string.e_ticket_cancellation_policy_description)
                    ))
                }
        val descriptionSpan = SpannableStringBuilder(SpannedString(getString(R.string.add_guests_descriptions)))
                .apply {
                    append('\n')
                    val span = SpannableString(getString(R.string.see_details))
                            .apply {
                                setSpan(object : ClickableSpan() {
                                    override fun onClick(widget: View?) {
                                        showBottomFragment(SheetData(
                                                error = getString(R.string.free_guest_convenience),
                                                title = getString(R.string.free_guest_policy),
                                                description = getString(R.string.free_guest_policy_description)
                                        ))
                                    }

                                    override fun updateDrawState(ds: TextPaint) {
                                        ds.isUnderlineText = false
                                    }

                                }, 0, length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
                                setSpan(TextAppearanceSpan(context, R.style.SeeDetails), 0, length, SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                    append(span)
                }
        description.movementMethod = LinkMovementMethod.getInstance()
        description.text = descriptionSpan
        continueButton.setOnClickListener {
            listener?.onGuestsContinue(ticketContainer.ticketPurchaseData)
        }
        closeButton.setOnClickListener {
            listener?.onClosePressed()
        }
        sofaIcon.setOnClickListener {
            showSeatPreviewFragment()
        }
        subscribe()
    }

    private fun showSeatPreviewFragment() {
        val set = TransitionSet()
        set.duration = 300
        val slide = Slide(Gravity.END);
        set.addTransition(slide);
        view?.let {
            val constraintSet = ConstraintSet()
            if (it is ConstraintLayout) {
                constraintSet.clone(it)
                TransitionManager.beginDelayedTransition(it, set)
                constraintSet.setVisibility(seatPreviewOntainer.id, View.VISIBLE)
                constraintSet.applyTo(it)
                childFragmentManager
                        .beginTransaction()
                        .replace(R.id.seatPreviewOntainer, SeatPreviewFragment.newInstance(SeatPreviewPayload(
                                screening = selectSeatPayload?.screening,
                                theater = selectSeatPayload?.theater,
                                showtime = selectSeatPayload?.showtime
                        )))
                        .commit()
            }
        }
    }

    override fun onClose() {
        val set = TransitionSet()
        set.duration = 300
        val slide = Slide(Gravity.END);
        set.addTransition(slide);
        val view = view ?: return
        if (view is ConstraintLayout) {
            val cs = ConstraintSet()
            cs.clone(view)
            cs.setVisibility(seatPreviewOntainer.id, View.INVISIBLE)
            val listener = object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    val fragment = childFragmentManager.findFragmentById(seatPreviewOntainer.id)
                    fragment?.let {
                        childFragmentManager
                                .beginTransaction()
                                .remove(it)
                                .commit()
                    }

                }
            }
            set.addListener(listener)
            TransitionManager.beginDelayedTransition(view, set)
            cs.applyTo(view)
        }
    }


    private fun subscribe() {
        disposable = listener?.payload()
                ?.take(1)
                ?.subscribe({
                    selectSeatPayload = it
                    ticketContainer.constraints = TicketConstraint(0, it.screening?.maximumGuests
                            ?: 0, guestTicketTypes = it?.screening?.getAvailability(it.showtime)?.guestsTicketTypes,
                            ticketPurchaseData = it.ticketPurchaseData
                    )
                }, {

                })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        listener = null
    }

    private fun showBottomFragment(sheetData: SheetData) {
        MPBottomSheetFragment.newInstance(sheetData).show(fragmentManager, "")
    }
}

