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
        
    }
}
