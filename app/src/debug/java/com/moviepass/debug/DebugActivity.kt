package com.moviepass.debug

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mobile.reservation.*
import com.moviepass.R
import java.util.*

class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val reservation = CurrentReservationV2(
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
        reservationV.bind(reservation)
        startActivity(
                ReservationActivity.newInstance(context = this, reservation = reservation)
        )
        finish()
//        val barcodeLayout = findViewById<BarcodeLayout>(R.id.barcode_bl)
//        barcodeLayout.visibility = View.VISIBLE
//        barcodeLayout.bind("0F34", BarcodeFormat.QR_CODE)
    }
}
