package com.mobile.reservation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mobile.model.ScreeningToken
import com.moviepass.R
import kotlinx.android.synthetic.main.activity_reservation.*

class ReservationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val windowParams = window.attributes
        windowParams.screenBrightness = 1.0f
        window.attributes = windowParams
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)
        val reservation: CurrentReservationV2? = intent.getParcelableExtra(KEY_RESERVATION)
        reservation?.let {
            reservationV.bind(it)
        }
        reservationV.setOnCloseListener(object : OnCloseListener {
            override fun onClose() {
                finish()
            }
        }
        )
    }

    companion object {

        const val KEY_RESERVATION = "reservation"

        fun newInstance(context: Context, reservation: CurrentReservationV2): Intent {
            return Intent(context, ReservationActivity::class.java).apply {
                putExtra(KEY_RESERVATION, reservation)
            }
        }

        fun newInstance(context: Context, reservation: ScreeningToken): Intent {
            return Intent(context, ReservationActivity::class.java).apply {
                val reservationV2 = CurrentReservationV2(
                        ticket = ETicket(
                                confirmationCodeFormat = reservation.confirmationCode.confirmationCodeFormat,
                                redemptionCode = reservation.confirmationCode.confirmationCode
                        ),
                        landscapeUrl = reservation.screening.landscapeImageUrl,
                        latitude = reservation.theater.lat,
                        longitude = reservation.theater.lon,
                        title = reservation.screening.title,
                        theater = reservation.screening.theaterName
                )
                putExtra(KEY_RESERVATION, reservationV2)
            }
        }
    }

}