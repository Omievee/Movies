package com.mobile.ticketverification

import com.mobile.camera.BarcodeData
import com.mobile.reservation.CurrentReservationV2
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

val NON_ALPHA_REGEX = "[^A-Za-z0-9]".toRegex()
val MONEY_REGEX = "($\\d+\\.\\d{1,2})".toRegex()
val RECEIPT = "RECEIPT"

data class VerificationData(
        var movieTitle: String? = null,
        var movieTime: String? = null,
        var movieDate: String? = null,
        var purchaseDate: String? = null,
        var purchaseTime: String? = null,
        var ticketPrice: String? = null,
        var theaterName: String? = null,
        var receipt: String? = null,
        var barcode: BarcodeData? = null
) {

    val showtimeFormats: List<DateFormat> by lazy {
        val hourFormats = arrayOf("h", "H", "HH", "hh")
        val minuteFormats = arrayOf("m", "mm")
        val ampmFormats = arrayOf("a", "")
        val combos = mutableListOf<SimpleDateFormat>()

        hourFormats.forEach { hour ->
            minuteFormats.forEach { minute ->
                ampmFormats.forEach { am ->
                    val format = "$hour$minute$am"
                    combos.add(SimpleDateFormat(format, Locale.US))
                }
            }
        }
        combos
    }

    val showDateFormats: List<DateFormat> by lazy {
        val month = arrayOf("M", "MM", "MMM", "")
        val dayFormats = arrayOf("d", "dd", "ddd", "dddd")
        val yearFormats = arrayOf("yy", "yyyy", "")
        val combos = mutableListOf<SimpleDateFormat>()

        month.forEach { month ->
            dayFormats.forEach { day ->
                yearFormats.forEach { year ->
                    val format1 = "$month$day$year"
                    combos.add(SimpleDateFormat(format1, Locale.US))
                }
            }
        }
        combos
    }

    fun update(reservation: CurrentReservationV2, text: TextBlock?) {
        val t = text ?: return
        movieTitle(reservation, t)
        theaterName(reservation, t)
        price(reservation, t)
        showtime(reservation, t)
        showdate(reservation, t)
        receipt(t)
    }

    fun update(barcode: BarcodeData) {
        this.barcode = barcode
    }

    private fun showtime(reservation: CurrentReservationV2, text: TextBlock) {
        if (movieTime != null) {
            return
        }
        val time = text.text?.sanitize() ?: return
        val showtime = reservation.showtime ?: return
        showtimeFormats.forEach {
            val newTxt = it.format(showtime).sanitize()
            if (time.contains(newTxt)) {
                println("we have a match ${time} ${text.text}")
                this.movieTime = text.text
            }
        }
    }

    private fun showdate(reservation: CurrentReservationV2, text: TextBlock) {
        if (movieDate != null) {
            return
        }
        val time = text.text?.sanitize() ?: return
        val showtime = reservation.showtime ?: return
        showtimeFormats.forEach {
            val newTxt = it.format(showtime).sanitize()
            if (time.contains(newTxt)) {
                println("we have a match ${time} ${text.text}")
                this.movieDate = text.text
            }
        }
    }

    private fun receipt(text: TextBlock) {
        val txt = text.text?.sanitize()
        if (receipt != null) {
            if (txt == RECEIPT) {
                receipt = text.text
            }
        }
    }

    private fun price(reservation: CurrentReservationV2, text: TextBlock) {

    }


    private fun theaterName(reservation: CurrentReservationV2, text: TextBlock) {
        when (theaterName != null) {
            true -> return
        }
        val name = reservation.theater?.sanitize() ?: return
        when (name.isEmpty()) {
            true -> return
        }
        val result = text.text?.sanitize() ?: return
        when (result.isEmpty()) {
            true -> return
        }
        if (name == result) {
            theaterName = text.text
        }
    }

    private fun movieTitle(reservation: CurrentReservationV2, text: TextBlock) {
        val title = reservation.title ?: return
        if (movieTitle == title) {
            return
        }
        val scannedText = text.text ?: return
        val preferredTitlesToMatch = arrayOf(title)
        preferredTitlesToMatch.forEach {
            if (it.toUpperCase() == scannedText.toUpperCase()) {
                this.movieTitle = it
                return@forEach
            }
        }
    }
}

fun String.sanitize(): String {
    return this.replace(com.mobile.ticketverification.NON_ALPHA_REGEX, "").toUpperCase()
}

// TICKET PRICE
// $10 $10.0 $10.00

// MOVIE DATE  ? may be below the movie title
// MM/dd/yy
// M/d/yy