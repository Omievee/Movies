package com.mobile.reservation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.fragments.MPFragment
import com.mobile.model.ScreeningToken
import com.mobile.model.Surge
import com.mobile.responses.ReservationResponse
import com.mobile.seats.BringAFriendActivity
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_reservation_checkin_bottom_sheet.*
import javax.inject.Inject
import com.mobile.activities.ActivateMoviePassCard
import com.mobile.adapters.toBold
import com.mobile.model.CappedPlan
import com.mobile.model.TicketType
import com.mobile.network.RestrictionsCheckResponse
import com.mobile.responses.PeakPass
import com.mobile.responses.PeakPassInfo
import com.mobile.seats.SheetData
import com.mobile.utils.showBottomFragment
import com.mobile.utils.text.centsAsDollars
import com.mobile.widgets.MPAlertDialog
import com.mobile.utils.text.toCurrency

class CheckInFragment : MPFragment(), CheckInFragmentView {
    override fun showOverCap(cappedPlan: CappedPlan?, it: RestrictionsCheckResponse) {
        val context = activity?:return
        MPAlertDialog(context)
                .setTitle(it.data.attributes?.title)
                .setMessage(it.data.attributes?.message)
                .setPositiveButton(R.string.continue_button) { _, _->
                    presenter.onContinueDialogClicked(it)
                }
                .setNegativeButton(R.string.go_back,null).show()
    }

    override fun showActivateCard(checkin: Checkin) {
        val activity: Activity = activity ?: return
        val activateCard = Intent(activity, ActivateMoviePassCard::class.java)
        activateCard.putExtra(Constants.SCREENING, checkin.screening)
        activateCard.putExtra(Constants.SHOWTIME, checkin.availability.startTime)
        startActivity(activateCard)
    }

    override fun showSoftCapMessage(checkin: Checkin, cappedPlan: CappedPlan) {
        val context = activity ?: return
        val span = SpannableStringBuilder()
        span.append(resources.getString(R.string.capped_plan_already_seen_1_of_3, cappedPlan.used))
        span.append(' ')
        span.append(cappedPlan.asDollars.toCurrency().toBold(context))
        span.append(' ')
        span.append(resources.getString(R.string.capped_plan_already_seen_2_of_3).toBold(context))
        span.append(' ')
        span.append(resources.getString(R.string.capped_plan_already_seen_3_of_3))
        continueDescription.text = span
        continueOrCheckin.text = when (checkin.availability.ticketType) {
            TicketType.STANDARD -> R.string.continue_button
            else -> R.string.continue_to_eticketing
        }
        continueDescription.visibility = View.VISIBLE
        continueOrCheckin.setOnClickListener {
            presenter.onContinueClicked()
        }
    }

    override fun navigateTo(checkIn: Checkin, reservation: ReservationResponse) {
        val activity = activity ?: return
        activity.onBackPressed()
        startActivity(ReservationActivity.newInstance(activity,
                ScreeningToken(
                        checkIn = checkIn,
                        reservation = reservation
                )))
    }

    override fun navigateToSoftCapCheckout(checkin: Checkin) {
        val activity = activity ?: return
        startActivityForResult(BringAFriendActivity.newIntent(activity, checkin), Constants.SOFT_CAP_CHECKOUT)
    }


    override fun navigateToSurchargeConfirm(checkin: Checkin) {
        val activity = activity ?: return
        startActivityForResult(BringAFriendActivity.newIntent(activity, checkin), Constants.SURGE_CHECKOUT_CODE)
    }

    override fun showNowPeakingNoPeakPass(checkin: Checkin, surge: Surge) {
        val context = activity ?: return
        MPAlertDialog(context)
                .setTitle(R.string.peak_pricing)
                .setMessage(getString(R.string.peak_no_pass_apply_surcharge, surge.costAsDollars))
                .setPositiveButton(R.string.apply_peak_pass) { _, _ ->
                    presenter.onApplyPeakPassClicked()
                }
                .setNegativeButton(R.string.go_back, null)
                .setNeutralButton(R.string.save_peak_pass_for_later) { _, _ ->
                    presenter.onSavePeakPassForLaterClicked()
                }.show()
    }

    override fun showApplyPeakPass(checkin: Checkin, peakPasses: PeakPassInfo, currentPeakPass: PeakPass?) {
        val context = activity ?: return
        MPAlertDialog(context)
                .setTitle(R.string.peak_pass)
                .setMessage(R.string.peak_pass_apply)
                .setPositiveButton(R.string.apply_peak_pass) { _, _ ->
                    presenter.onApplyPeakPassClicked()
                }
                .setNegativeButton(R.string.go_back, null)
                .setNeutralButton(R.string.save_peak_pass_for_later) { _, _ ->
                    presenter.onSavePeakPassForLaterClicked()
                }.show()
    }

    override fun showPeakPassSheet(checkin: Checkin, peak: PeakPassInfo, peakPass: PeakPass?) {
        showBottomFragment(
                SheetData(
                        title = getString(R.string.peak_pass),
                        description = getString(R.string.peak_pass_description),
                        subDescription = when (peakPass) {
                            null -> when (peak.nextRefillDate) {
                                null -> null
                                else -> resources.getString(R.string.next_pass_applied, peak.nextRefillDate)
                            }
                            else -> resources.getString(R.string.peak_pass_expires, peakPass.expiresAsString())
                        },
                        gravity = Gravity.CENTER
                )
        )
    }

    override fun showNowPeakingApplyPeakPass(it: RestrictionsCheckResponse, peak: PeakPassInfo, peakPass: PeakPass) {
        val context = activity ?: return
        MPAlertDialog(context)
                .setTitle(R.string.peak_pricing)
                .setMessage(resources.getString(R.string.peak_pass_apply_surcharge, it.data.attributes?.peakAmount?.centsAsDollars?:""))
                .setPositiveButton(R.string.apply_peak_pass) { _, _ ->
                    presenter.onApplyPeakPassClicked()
                }
                .setNegativeButton(R.string.go_back, null)
                .setNeutralButton(R.string.save_peak_pass_for_later) { _, _ ->
                    presenter.onSavePeakPassForLaterClicked()
                }.show()
    }

    override fun showSurgeModal(it:RestrictionsCheckResponse) {
        val context = activity ?: return
        MPAlertDialog(context)
                .setTitle(R.string.peak_pricing)
                .setMessage(context.getString(R.string.reservation_surge_description, it.data.attributes?.peakAmount?.centsAsDollars?:""))
                .setPositiveButton(R.string.continue_button) { _, _ ->
                    presenter.onContinueDialogClicked(it)
                }
                .setNegativeButton(R.string.go_back, null)
                .show()
    }

    override fun showProgress() {
        activity?:return
        continueOrCheckin.progress = true
    }

    override fun hideProgress() {
        activity?:return
        continueOrCheckin.progress = false
    }

    override fun showNeedLocation() {

    }

    override fun showError(apiError: ApiError) {
        val context = activity ?: return
        MPAlertDialog(context)
                .setMessage(apiError.error.message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun showGenericError() {
        val context = activity ?: return
        MPAlertDialog(context)
                .setMessage(R.string.error)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun navigateToCreateReservation(checkin: Checkin) {
        val activity = activity ?: return
        startActivityForResult(BringAFriendActivity.newIntent(activity, checkin), Constants.SURGE_CHECKOUT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            Constants.SURGE_CHECKOUT_CODE, Constants.SOFT_CAP_CHECKOUT -> {
                parentFragment?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun showCheckin(checkin:Checkin) {
        continueOrCheckin.apply {
            continueOrCheckin.text = when (checkin.availability.ticketType) {
                TicketType.STANDARD -> R.string.check_in_button
                else -> R.string.continue_to_eticketing
            }
            setOnClickListener {
                presenter.onContinueClicked()
            }
        }
    }

    override fun showCheckinWithProof() {
        continueOrCheckin.apply {
            setOnClickListener {
                showTicketVerificationDialog()
            }
        }
    }

    private fun showTicketVerificationDialog() {
        val context = activity ?: return
        MPAlertDialog(context)
                .setTitle(R.string.ticket_verification)
                .setMessage(getString(R.string.pre_pop_dialog) + getString(R.string.pre_pop_dialog2))
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    presenter.onContinueClicked()
                }).show()
    }

    override fun showWillSurge(surge: Surge, peakPassInfo: PeakPassInfo, peakPass: PeakPass?) {
        continueDescription.apply {
            visibility = View.VISIBLE
            text = when (surge.amount == 0) {
                true -> getString(R.string.reservation_will_surge_unknown_description)
                false -> {
                    val span = SpannableStringBuilder()
                    span.append(resources.getString(R.string.reservation_will_surge_start))
                    span.append(' ')
                    span.append(surge.costAsDollars.toBold(context))
                    span.append(' ')
                    span.append(resources.getString(R.string.reservation_will_surge_end))
                    span
                }
            }
        }
        continueOrCheckin.apply {
            text = R.string.continue_button
            setOnClickListener {
                presenter.onContinueClicked()
            }
        }

        handlePeakPass(peakPassInfo)
    }

    private fun handlePeakPass(peakPassInfo: PeakPassInfo) {
        when {
            !peakPassInfo.enabled -> {
                peakPassContainer.visibility = View.GONE
            }
            else -> {
                peakPassContainer.visibility = View.VISIBLE
                peakPassContainer.setOnClickListener {
                    presenter.onPeakPassInfoClicked()
                }
                peakPassDescription.apply {
                    text = when (peakPassInfo.peakPasses.size) {
                        0 -> resources.getString(R.string.no_peak_passes_left)
                        else -> resources.getQuantityString(R.plurals.peak_passes_left, peakPassInfo.peakPasses.size, peakPassInfo.peakPasses.size)
                    }
                }
            }
        }
    }

    override fun showSurge(surge: Surge, peakPassInfo: PeakPassInfo, peakPass: PeakPass?) {
        continueDescription.apply {
            visibility = View.VISIBLE
            val span = SpannableStringBuilder()
            span.append(resources.getString(R.string.reservation_surge_start))
            span.append(' ')
            span.append(surge.costAsDollars.toBold(context))
            span.append(' ')
            span.append(resources.getString(R.string.reservation_surge_end))
            text = span
        }
        continueOrCheckin.apply {
            text = R.string.continue_button
            setOnClickListener {
                presenter.onContinueClicked()
            }
        }
        handlePeakPass(peakPassInfo)
    }

    var checkin: Checkin? = null

    @Inject
    lateinit var presenter: CheckInFragmentPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reservation_checkin_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkin = arguments?.getParcelable("payload")
        presenter.onCreate(checkin)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}

fun newInstance(checkin: Checkin): CheckInFragment {
    val f = CheckInFragment().apply {
        arguments = Bundle().apply {
            putParcelable("payload", checkin)
        }
    }
    return f
}
