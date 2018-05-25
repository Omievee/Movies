package com.moviepass.debug

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mobile.Constants
import com.mobile.activities.ConfirmationActivity
import com.mobile.activities.TheatersActivity
import com.mobile.model.Reservation
import com.mobile.model.Screening
import com.mobile.model.ScreeningToken
import com.mobile.reservation.*
import com.mobile.responses.ReservationResponse
import com.moviepass.R
import org.parceler.Parcels
import java.text.SimpleDateFormat
import java.util.*

class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val r = CurrentReservationV2(
                ticket = ETicket(TicketFormat.QRCODE.name,
                        "5382xs2", "A9"),
                reservation = Reservation2(
                  id=1234,
                    _showtime = System.currentTimeMillis()
                ),
                theater = "FOO GOOD RICH LONG THEATHER",
                title = "A WRINKLE IN TIME",
                landscapeUrl = "http://a1.moviepass.com/posters/landscape/AVENGERSINFINITYWAR.jpg"
        )
        val reservationV = findViewById<ReservationView>(R.id.reservationV)
        //reservationV.bind(reservation)
        val screening = Screening()
        screening.theaterName = r.theater
        screening.title = r.title
        screening.moviepassId = r.reservation!!.moviepassId
        screening.tribuneTheaterId = r.reservation!!.tribuneTheaterId
        var confirmation: ReservationResponse.ETicketConfirmation? = null
        if (r.ticket != null) {
            confirmation = ReservationResponse.ETicketConfirmation()
            confirmation.confirmationCode = r.ticket!!.redemptionCode
            confirmation.barCodeUrl = ""
        }
        var reservation: Reservation? = null
        if (r.reservation != null) {
            reservation = Reservation()
            reservation.id = r.reservation!!.id!!
        }
        val token = ScreeningToken(
                screening,
                SimpleDateFormat("h:mm a").format(r.showtime),
                reservation,
                confirmation,
                null
        )
        val intent = Intent(this, ConfirmationActivity::class.java).putExtra(Constants.TOKEN, Parcels.wrap(token))
        startActivity(Intent(this, TheatersActivity::class.java))
        finish()
    }
}
