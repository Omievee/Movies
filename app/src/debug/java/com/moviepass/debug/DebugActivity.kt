package com.moviepass.debug

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mobile.model.GuestTicket
import com.mobile.model.GuestTicketType
import com.mobile.model.SeatInfo
import com.mobile.reservation.CurrentReservationV2
import com.mobile.reservation.ETicket
import com.mobile.reservation.Reservation2
import com.mobile.reservation.TicketFormat
import com.mobile.seats.*
import com.moviepass.R
import kotlinx.android.synthetic.debug.activity_debug.*
import java.text.SimpleDateFormat

class DebugActivity : AppCompatActivity(), SeatPreviewListener {
    override fun onClose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

//        reservationView
//                .bind(showCurrentReservationText = true,
//                        reservation = CurrentReservationV2(
//                                reservation = Reservation2(_showtime = SimpleDateFormat("hh:mm a").parse("9:00 PM").time
//                                ),
//                                ticket = ETicket(TicketFormat.QRCODE.name, "MGU6RW", seats=listOf("D1","D2")),
//                                landscapeUrl = "https://a1.moviepass.com/posters/landscape/DEADPOOL2.jpg",
//                                theater = "Studio Movie Grill Arlington",
//                                title = "Deadpool 2"
//                        )
//                )
    }
}
