package com.mobile.surge

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.billing.MissingBillingFragment
import com.mobile.fragments.MPFragment
import com.mobile.model.*
import com.mobile.requests.TicketInfoRequest
import com.mobile.reservation.Checkin
import com.mobile.reservation.ReservationActivity
import com.mobile.seats.BringAFriendListener
import com.mobile.seats.SelectSeatPayload
import com.mobile.seats.SheetData
import com.mobile.session.UserManager
import com.mobile.tickets.TicketManager
import com.mobile.utils.showBottomFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_confirm_surcharge.*
import javax.inject.Inject

class ConfirmSurgeFragment : MPFragment() {

    @Inject
    lateinit var locationManager: com.mobile.location.LocationManager

    @Inject
    lateinit var ticketManager: TicketManager

    @Inject
    lateinit var sessionManager: UserManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    var listener: BringAFriendListener? = null

    var disposable: Disposable? = null
    var userInfoDisposable: Disposable? = null
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
        userInfoDisposable?.dispose()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_confirm_surcharge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submit.setOnClickListener {
            val ticketType: TicketType = payload?.screening?.getTicketType() ?: return@setOnClickListener
            when (isTicketVerification() && ticketType == TicketType.STANDARD){
                true -> showTicketVerificationDialog()
                false -> reserveTicket()
            }
        }
        closeButton
                .setOnClickListener {
                    listener?.onClosePressed()
                }

        backButton
                .setOnClickListener {
                    listener?.onBackPressed()
                }
        surgeCancellationAndRefundPolicy.setOnClickListener {
            showBottomFragment(SheetData(
                    error = "",
                    title = getString(R.string.surge_cancellation),
                    description = getString(R.string.surge_cancellation_description)
            ))
        }
        subscribe()
    }

    private fun showPaymentMethodDeclined() {
        AlertDialog.Builder(context)
                .setTitle(R.string.update_payment_method)
                .setMessage(R.string.update_payment_method_description)
    }

    val clickListener = View.OnClickListener {
        showFragment(MissingBillingFragment())
    }

    val infoClickListener = object : InfoClickListener {
        override fun onClickInfo() {
            val activity = activity ?: return
            startActivity(PeakPricingActivity.newInstance(activity))
        }

    }

    private fun isTicketVerification() : Boolean{
        return UserPreferences.restrictions.proofOfPurchaseRequired
    }

    private fun subscribe() {
        disposable?.dispose()
        disposable = listener?.payload()
                ?.subscribe({
                    payload = it
                    moviePosterHeader.bind(it)
                    val surge = it.screening?.getSurge(it.availability?.startTime, UserPreferences.restrictions.userSegments)
                            ?: return@subscribe
                    surgeTicket.bind(surge, infoClickListener = infoClickListener)
                    surgeTotal.bind(surge)
                    surgeTotal.setOnClickListener(clickListener)
                    submit.isEnabled = true
                    fetchUserInfo()
                }, {
                })
    }

    private fun fetchUserInfo() {
        userInfoDisposable?.dispose()
        userInfoDisposable = sessionManager
                .getUserInfo()
                .subscribe({ info ->
                    val availability = payload?.availability ?: return@subscribe
                    val surge = payload?.screening?.getSurge(availability.startTime, UserPreferences.restrictions.userSegments)
                            ?: return@subscribe
                    surgeTotal.bind(surge, info)
                }, {

                })
    }

    private fun showTicketVerificationDialog() {
        val context = activity?:return
        android.support.v7.app.AlertDialog.Builder(context,R.style.CUSTOM_ALERT)
                .setView(R.layout.alertdialog_ticketverif)
                .setPositiveButton(android.R.string.ok, {_,_ ->
                    reserveTicket()
                }).show()
    }

    private fun reserveTicket() {
        val screening: Screening = payload?.screening ?: return
        val theater: Theater = payload?.theater ?: return
        val availability: Availability = payload?.availability ?: return
        val info = availability.providerInfo ?: return
        val lat = locationManager.lastLocation() ?: return
        submit.progress = true
        val checkIn = Checkin(
                screening = screening,
                theater = theater,
                availability = availability)
        ticketManager
                .reserve(checkin = checkIn,
                        ticketRequest = TicketInfoRequest(
                                performanceInfo = info,
                                latitude = lat.lat,
                                longitude = lat.lon)
                )
                .doOnSubscribe {
                    analyticsManager.onCheckinAttempt(checkIn)
                }
                .doAfterTerminate({ submit.progress = false })
                .subscribe({
                    val activity = activity ?: return@subscribe
                    analyticsManager.onCheckinSuccessful(checkIn, it)
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()

                    startActivity(ReservationActivity
                            .newInstance(activity, ScreeningToken(
                                    checkIn = checkIn,
                                    reservation = it)
                            ))
                }, {
                    val error = it as? ApiError ?: return@subscribe
                    showError(error)
                    analyticsManager.onCheckinFailed(checkIn)
                })

    }

    private fun showError(apiError: ApiError) {
        val context = activity ?: return
        AlertDialog.Builder(context)
                .setMessage(apiError.error.message)
                .setPositiveButton(android.R.string.ok, null)
                .show()

    }
}