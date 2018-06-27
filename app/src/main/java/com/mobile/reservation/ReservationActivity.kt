package com.mobile.reservation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.mobile.MPActivty
import com.mobile.UserPreferences
import com.mobile.fragments.TicketVerificationV2
import com.mobile.model.PopInfo
import com.mobile.model.ScreeningToken
import com.mobile.utils.onBackExtension
import com.mobile.utils.showFragment
import com.moviepass.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_reservation.*
import kotlinx.android.synthetic.main.layout_current_reservation.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReservationActivity : MPActivty() {

    @Inject
    lateinit var presenter: ReservationActivityPresenter

    private var canClose: Boolean = false

    private var reservation:CurrentReservationV2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        val windowParams = window.attributes
        windowParams.screenBrightness = 1.0f
        window.attributes = windowParams
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)
        reservation = intent.getParcelableExtra(KEY_RESERVATION)

        val showCurrentReservationText = intent.getBooleanExtra(KEY_SHOW_CURRENT_RESERVATION_TEXT, false)
        canClose = intent.getBooleanExtra(KEY_CAN_CLOSE, false)
        reservation?.let {
            if(UserPreferences.zipCode == null)
                presenter.getUserZipCode()
            reservationV.bind(it, showCurrentReservationText, canClose)
        }
        reservationV.setOnCloseListener(object : OnCloseListener {
            override fun openTicketVerificationFragment() {
                reservation?.let {
                    showFragment(TicketVerificationV2.newInstance(it.toPop(),false))
                }
            }

            override fun cancelReservation(reservationId: Int) {
                if(reservationId == 0){
                    Toast.makeText(reservationV.context,"Can't cancel reservation, try again later.",Toast.LENGTH_SHORT).show()
                }else{
                    presenter.cancelCurrentReservation(reservationId)
                }
            }

            override fun onClose() {
                finish()
            }
        }
        )
    }

    fun CurrentReservationV2.toPop() : PopInfo {
        return PopInfo(
                reservationId = reservation?.id?:0,
                movieTitle= title,
                theaterName = theater,
                tribuneMovieId = reservation?.tribuneTheaterId.toString(),
                showtime = showtime.toString(),
                tribuneTheaterId = reservation?.moviepassId.toString())
    }

    override fun onBackPressed() {
        onBackExtension()
        val canGoBack = UserPreferences.restrictions?.proofOfPurchaseRequired==false || reservation?.ticket?.redemptionCode!=null
        if(canGoBack)
            super.onBackPressed()
        else{
            if(canClose)
                super.onBackPressed()
        }
    }

    fun hideProgress(){
        reservationV.progress.visibility = View.GONE
    }


    companion object {

        const val KEY_RESERVATION = "reservation"
        const val KEY_SHOW_CURRENT_RESERVATION_TEXT = "show_as_current_reservation"
        const val KEY_CAN_CLOSE = "can_close"

        fun newInstance(context: Context, reservation: CurrentReservationV2, canClose: Boolean = false): Intent {
            return Intent(context, ReservationActivity::class.java).apply {
                putExtra(KEY_RESERVATION, reservation)
                putExtra(KEY_SHOW_CURRENT_RESERVATION_TEXT, false)
                putExtra(KEY_CAN_CLOSE, canClose)
            }
        }

        fun newInstance(context: Context, reservation: ScreeningToken, canClose: Boolean = false): Intent {
            return Intent(context, ReservationActivity::class.java).apply {
                val rs = reservation.reservation
                val seatsToUse:List<String>? = reservation.seatSelected?.map { it.seatName }?: rs.seats
                val re2:Reservation2? = rs?.let {
                    Reservation2(
                            checkinId = rs.id,
                            createdAt = rs.expiration,
                            id = rs.id,
                            _showtime = reservation.availability?.startTime?.let {
                                try {
                                    SimpleDateFormat("hh:mm a", Locale.US).parse(it).time
                                } catch (e:Error) {0L}
                            }?:0
                    )
                }
                var ticket : ETicket? = null
                if(reservation.confirmationCode!=null){
                    ticket = ETicket(
                            confirmationCodeFormat = reservation.confirmationCode?.confirmationCodeFormat,
                            redemptionCode = reservation.confirmationCode?.confirmationCode,
                            seats = seatsToUse
                    )
                }
                val reservationV2 = CurrentReservationV2(
                        ticket,
                        reservation = re2,
                        landscapeUrl = reservation.screening.landscapeImageUrl,
                        latitude = reservation.theater?.lat,
                        longitude = reservation.theater?.lon,
                        title = reservation.screening.title,
                        theater = reservation.theater?.name?:reservation.screening.theaterName
                )
                putExtra(KEY_RESERVATION, reservationV2)
                putExtra(KEY_SHOW_CURRENT_RESERVATION_TEXT, true)
                putExtra(KEY_CAN_CLOSE, canClose)
            }
        }
    }

}