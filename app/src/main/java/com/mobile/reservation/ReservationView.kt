package com.mobile.reservation

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.View
import com.google.zxing.BarcodeFormat
import com.mobile.utils.MapUtil
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_current_reservation.view.*
import java.text.SimpleDateFormat
import java.util.*
import com.mobile.utils.startIntentIfResolves

class ReservationView(context: Context, attributeSet: AttributeSet?) : ConstraintLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.layout_current_reservation, this)
    }

    fun bind(reservation: CurrentReservationV2, showCurrentReservationText: Boolean = true) {
        movieName.text = reservation.title
        theaterName.text = reservation.theater
        movieShowtime.text = reservation.showtime?.let {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
        }
        seats.text = reservation.ticket?.seat?.let {
            SpannableStringBuilder().apply {
                if (it.isNotEmpty()) {
                    append(resources.getQuantityText(R.plurals.seat, 1))
                    append(' ')
                    append(it)
                }
            }
        }
        if (seats.text.isEmpty()) {
            seats.visibility = View.GONE
        } else {
            seats.visibility = View.VISIBLE
        }
        moviePoster.setImageURI(reservation.landscapeUrl)
        arrayOf(theaterName, theaterPin).forEach {
            it.setOnClickListener {
                val latitude = reservation.latitude;
                val longitude = reservation.longitude;
                if (latitude != null && longitude != null) {
                    context.startIntentIfResolves(MapUtil.mapIntent(latitude, longitude))
                }

            }
        }
        reservation.ticket?.let {
            val barcodeFormat = when (it.format) {
                TicketFormat.QRCODE -> {
                    BarcodeFormat.QR_CODE
                }
                TicketFormat.BARCODE -> {
                    BarcodeFormat.CODE_128
                }
                else -> {
                    null
                }
            }
            val redemptionCode = it.redemptionCode
            if (barcodeFormat != null && redemptionCode != null) {
                barcodeL.visibility = View.VISIBLE
                codeCL.visibility = View.VISIBLE
                barcodeL.bind(redemptionCode, barcodeFormat)
            } else {
                codeCL.visibility = View.GONE
                barcodeL.visibility = View.GONE
            }

            when (it.format == TicketFormat.UNKNOWN) {
                true -> reservationCode.background = null
            }
            reservationCode.text = it.redemptionCode
        } ?: run {
            reservationDescriptionTV.setText(R.string.reservation_use_card_description)
            creditCardIV.visibility = View.VISIBLE
            reservationCode.background = null
        }

        currentReservationTV.visibility = when(showCurrentReservationText) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    fun setOnCloseListener(onCloseListener: OnCloseListener?) {
        closeIV.setOnClickListener {
            onCloseListener?.onClose()
        }
    }

}

interface OnCloseListener {
    fun onClose()
}