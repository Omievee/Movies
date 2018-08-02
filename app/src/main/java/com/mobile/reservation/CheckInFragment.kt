package com.mobile.reservation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
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
import com.mobile.network.SurgeResponse
import com.mobile.responses.PeakPass
import com.mobile.responses.PeakPassInfo
import com.mobile.seats.SheetData
import com.mobile.utils.showBottomFragment
import com.mobile.utils.text.centsAsDollars

class CheckInFragment : MPFragment(), CheckInFragmentView {

    override fun showActivateCard(checkin: Checkin) {
        val activity:Activity = activity?:return
        val activateCard = Intent(activity, ActivateMoviePassCard::class.java)
        activateCard.putExtra(Constants.SCREENING, checkin.screening)
        activateCard.putExtra(Constants.SHOWTIME, checkin.availability.startTime)
        startActivity(activateCard)
    }

    override fun navigateTo(checkIn: Checkin, reservation: ReservationResponse) {
        val activity = activity?:return
        activity.onBackPressed()
        startActivity(ReservationActivity.newInstance(activity,
                ScreeningToken(
                        checkIn = checkIn,
                        reservation=reservation
                )))
    }

    override fun navigateToSurchargeConfirm(checkin: Checkin) {
        val activity = activity?:return
        startActivityForResult(BringAFriendActivity.newIntent(activity,checkin), Constants.SURGE_CHECKOUT_CODE)
    }

    override fun showNowPeakingNoPeakPass(checkin: Checkin, surge: Surge) {
        val context = activity?:return
        AlertDialog.Builder(context)
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
        val context = activity?:return
        AlertDialog.Builder(context)
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
                        subDescription = when(peakPass) {
                            null-> when(peak.nextRefillDate) {
                                null-> null
                                else-> resources.getString(R.string.next_pass_applied, peak.nextRefillDate)
                            }
                            else-> resources.getString(R.string.peak_pass_expires, peakPass.expiresAsString())
                        },
                        gravity = Gravity.CENTER
                )
        )
    }

    override fun showNowPeakingApplyPeakPass(it: SurgeResponse, peak: PeakPassInfo, peakPass: PeakPass) {
        val context = activity?:return
        AlertDialog.Builder(context)
                .setTitle(R.string.peak_pricing)
                .setMessage(resources.getString(R.string.peak_pass_apply_surcharge, it.peakAmount.centsAsDollars))
                .setPositiveButton(R.string.apply_peak_pass) { _, _ ->
                    presenter.onApplyPeakPassClicked()
                }
                .setNegativeButton(R.string.go_back, null)
                .setNeutralButton(R.string.save_peak_pass_for_later) { _, _ ->
                    presenter.onSavePeakPassForLaterClicked()
                }.show()
    }

    override fun showSurgeModal(peakAmount: String) {
        val context = activity?:return
        AlertDialog.Builder(context)
                .setTitle(R.string.peak_pricing)
                .setMessage(context.getString(R.string.reservation_surge_description,peakAmount))
                .setPositiveButton(R.string.continue_button) { _, _->
                    presenter.onContinueDialogClicked()
                }
                .setNegativeButton(R.string.go_back, null)
                .show()
    }

    override fun showProgress() {
        continueOrCheckin.progress = true
    }

    override fun hideProgress() {
        continueOrCheckin.progress = false
    }

    override fun showNeedLocation() {

    }

    override fun showError(apiError: ApiError) {
        val context = activity ?: return
        AlertDialog.Builder(context)
                .setMessage(apiError.error.message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun showGenericError() {
        val context = activity ?: return
        AlertDialog.Builder(context)
                .setMessage(R.string.error)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun showContinueToETicketing() {
        continueOrCheckin.apply {
            text = R.string.continue_to_eticketing
            setOnClickListener {
                presenter.onContinueToETicketingClicked()
            }
        }
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
            Constants.SURGE_CHECKOUT_CODE -> {
                parentFragment?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun showCheckin() {
        continueOrCheckin.apply {
            text = R.string.checkin
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
        val context = activity?:return
        AlertDialog.Builder(context,R.style.CUSTOM_ALERT)
        .setView(R.layout.alertdialog_ticketverif)
                .setPositiveButton(android.R.string.ok, {_,_ ->
                    presenter.onContinueClicked()
                }).show()
    }

    override fun showWillSurge(surge: Surge, peakPassInfo: PeakPassInfo, peakPass: PeakPass?) {
        continueDescription.apply {
            visibility = View.VISIBLE
            text = when (surge.amount == 0) {
                true -> getString(R.string.reservation_will_surge_unknown_description)
                false -> getString(R.string.reservation_will_surge_description, surge.costAsDollars)
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
            else-> {
                peakPassContainer.visibility = View.VISIBLE
                peakPassContainer.setOnClickListener {
                    presenter.onPeakPassInfoClicked()
                }
                peakPassDescription.apply {
                    text = resources.getQuantityString(R.plurals.peak_passes_left, peakPassInfo.peakPasses.size, peakPassInfo.peakPasses.size)
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
