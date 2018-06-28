package com.mobile.reservation

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.View
import com.google.zxing.BarcodeFormat
import com.mobile.UserPreferences
import com.mobile.utils.MapUtil
import com.mobile.utils.startIntentIfResolves
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_current_reservation.view.*
import java.text.SimpleDateFormat
import java.util.*

class ReservationView(context: Context, attributeSet: AttributeSet?) : ConstraintLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.layout_current_reservation, this)
    }

    var reservationId: Int? = null

    fun bind(reservation: CurrentReservationV2, showCurrentReservationText: Boolean = false, canClose: Boolean) {
        reservationId = reservation.reservation?.id
        movieName.text = reservation.title
        theaterName.text = reservation.theater
        movieShowtime.text = reservation.showtime?.let {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
        }
        seats.text = reservation.ticket?.seats?.let {
            SpannableStringBuilder().apply {
                if (it.isNotEmpty()) {
                    append(resources.getQuantityString(R.plurals.seat, it.size))
                    append(' ')
                    append(it.joinToString(", "))
                }
            }
        }

        when(canClose){
            true -> closeIV.visibility = View.VISIBLE
            false -> closeIV.visibility = View.INVISIBLE
        }

        reservationDescriptionTV.text = resources.getQuantityText(R.plurals.reservation_code_description, reservation.ticket?.seats?.size
                ?: 1)
        if (seats.text.isEmpty()) {
            seats.visibility = View.GONE
        } else {
            seats.visibility = View.VISIBLE
        }
        moviePosterHeader.setImageURI(reservation.landscapeUrl)
        arrayOf(theaterName, theaterPin).forEach {
            it.setOnClickListener {
                val latitude = reservation.latitude;
                val longitude = reservation.longitude;
                if (latitude != null && longitude != null) {
                    context.startIntentIfResolves(MapUtil.mapIntent(latitude, longitude))
                }
            }
        }
        if (!reservation.ticket?.redemptionCode.isNullOrEmpty()) {
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
                val middleCLTop: Int
                val reservationDescriptionBottom: Int
                if (barcodeFormat != null && redemptionCode != null) {
                    middleCLTop = codeCL.id
                    reservationDescriptionBottom = codeCL.id
                    barcodeL.visibility = View.VISIBLE
                    codeCL.visibility = View.VISIBLE
                    barcodeL.bind(redemptionCode, barcodeFormat)
                } else {
                    middleCLTop = reservationDescriptionTV.id
                    reservationDescriptionBottom = middleCL.id
                    codeCL.visibility = View.GONE
                    barcodeL.visibility = View.GONE
                }
//                val set = ConstraintSet()
//                set.clone(reservationCL)
//                set.connect(middleCL.id, ConstraintSet.TOP, middleCLTop, ConstraintSet.BOTTOM)
//                set.connect(reservationDescriptionBottom, ConstraintSet.BOTTOM, middleCL.id, ConstraintSet.TOP)
//                set.applyTo(reservationCL)
//                if (codeCL.visibility == View.GONE) {
//                    set.clone(middleCL)
//                    set.setVerticalBias(reservationCode.id, 0f)
//                    set.applyTo(middleCL)
//                }
                when (it.format == TicketFormat.UNKNOWN || it.format == TicketFormat.QRCODE) {
                    true -> reservationCode.background = null
                }
                reservationCode.visibility = View.VISIBLE
                reservationCode.text = it.redemptionCode
            }
        } else {
            run {

                if (UserPreferences.getProofOfPurchaseRequired() && UserPreferences.getLastReservationPopInfo()!=reservationId) {
                    ticketVerificationBanner.visibility = View.VISIBLE
                }

                reservationDescriptionTV.visibility = View.VISIBLE
                reservationDescriptionTV.text = resources.getString(R.string.reservation_description)
                creditCardIV.visibility = View.VISIBLE
                zipCodeDescription.apply {
                    setText(R.string.if_asked_provide_zip)
                    visibility = View.VISIBLE
                }
                ZipCodeNumberReservation.apply {
                    text = UserPreferences.getZipCode()
                    visibility = View.VISIBLE
                }
                cancelCurrentReservationTV.visibility = View.VISIBLE
            }
        }
    }

    fun setOnCloseListener(onCloseListener: OnCloseListener?) {
        closeIV.setOnClickListener {
            onCloseListener?.onClose()
        }

        cancelCurrentReservationTV.setOnClickListener {
            onCloseListener?.cancelReservation(reservationId ?: 0)
            progress.visibility = View.VISIBLE
        }

        ticketVerificationBanner.setOnClickListener {
            onCloseListener?.openTicketVerificationFragment()
        }
    }
}

interface OnCloseListener {
    fun onClose()
    fun cancelReservation(reservationId: Int)
    fun openTicketVerificationFragment()
}