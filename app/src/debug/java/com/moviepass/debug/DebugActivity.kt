package com.moviepass.debug

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mobile.model.TicketType
import com.mobile.reservation.CurrentReservationV2
import com.mobile.reservation.ETicket
import com.mobile.reservation.Reservation2
import com.mobile.reservation.ReservationActivity
import com.mobile.seats.SeatPreviewListener
import com.moviepass.R
import java.text.SimpleDateFormat

class DebugActivity : AppCompatActivity(), SeatPreviewListener {
    override fun onClose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        startActivity(ReservationActivity.newInstance(this, reservation = CurrentReservationV2(
                reservation = Reservation2(_showtime = SimpleDateFormat("hh:mm a").parse("9:00 PM").time
                ),
                ticket = ETicket(TicketType.E_TICKET.toString(), "MGU6RW",
                        seats = listOf("D1")),
                landscapeUrl = "https://a1.moviepass.com/posters/landscape/DEADPOOL2.jpg",
                theater = "Studio Movie Grill Arlington",
                title = "Deadpool 2"
        )))

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
