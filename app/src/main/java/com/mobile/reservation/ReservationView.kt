package com.mobile.reservation

import android.content.Context
import android.location.Location
import android.support.constraint.ConstraintLayout
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.View
import com.google.zxing.BarcodeFormat
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.fragments.toLocation
import com.mobile.location.LocationManager
import com.mobile.utils.MapUtil
import com.mobile.utils.startIntentIfResolves
import com.mobile.widgets.MPAlertDialog
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_current_reservation.view.*
import java.text.SimpleDateFormat
import java.util.*

class ReservationView(context: Context, attributeSet: AttributeSet?) : ConstraintLayout(context, attributeSet) {

    init {
        inflate(context, R.layout.layout_current_reservation, this)
    }

    var reservationId: Int? = null
    var locationManager: LocationManager? = null

    var isInLocation: Boolean = false
    var currentReservationV2: CurrentReservationV2? = null

    val canReveal: Boolean
        get() {
            val location = when (currentReservationV2) {
                null -> return false
                else -> Location("").apply {
                    latitude = currentReservationV2?.latitude ?: return false
                    longitude = currentReservationV2?.longitude ?: return false
                }
            }
            val lastLocation = locationManager?.lastLocation()?.toLocation() ?: return false
            return lastLocation.distanceTo(location) <= Constants.MINIMUM_CHECKIN_RADIUS_METERS
        }

    fun bind(reservation: CurrentReservationV2, showCurrentReservationText: Boolean = false, locationManager: LocationManager) {
        reservationId = reservation.reservation?.id
        currentReservationV2 = reservation
        this.locationManager = locationManager
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
            closeIV.visibility = View.VISIBLE
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
                    tapToRevealBarcode.visibility = View.VISIBLE
                    barcodeL.bind(barcode = "- - - - - -", type = barcodeFormat)
                    val onReveal = object : OnReveal {
                        override fun onReveal() {
                            tapToRevealBarcode.visibility = View.GONE
                            reservationCode.text = it.redemptionCode
                            barcodeL.bind(barcode = redemptionCode, type = barcodeFormat)
                        }

                        override fun onTooFarAway() {
                            showTooFarModal()
                        }

                    }
                    tapToRevealBarcode.setOnClickListener {
                        determineIfCanReveal(onReveal)
                    }

                } else {
                    codeCL.visibility = View.GONE
                    barcodeL.visibility = View.GONE
                }
                when (it.format == TicketFormat.UNKNOWN || it.format == TicketFormat.QRCODE) {
                    true -> {
                        reservationCode.background = null
                        tapToRevealCode.visibility = View.GONE
                    }
                    else-> {
                        tapToRevealCode.visibility=View.VISIBLE
                    }
                }
                reservationCode.visibility = View.VISIBLE
                val onReveal = object : OnReveal {
                    override fun onReveal() {
                        tapToRevealCode.visibility = View.GONE
                        reservationCode.text = it.redemptionCode
                    }

                    override fun onTooFarAway() {
                        showTooFarModal()
                    }

                }
                tapToRevealCode.setOnClickListener {
                    determineIfCanReveal(onReveal)
                }

                currentReservationTV.visibility = when (showCurrentReservationText) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
            }
        } else {
            run {

                if (UserPreferences.restrictions.proofOfPurchaseRequired && UserPreferences.lastReservationPopInfo != reservationId) {
                    ticketVerificationBanner.visibility = View.VISIBLE
                    closeIV.visibility = View.GONE
                }

                reservationDescriptionTV.visibility = View.VISIBLE
                reservationDescriptionTV.text = resources.getString(R.string.reservation_description)
                creditCardIV.visibility = View.VISIBLE
                zipCodeDescription.apply {
                    setText(R.string.if_asked_provide_zip)
                    visibility = View.VISIBLE
                }
                zipCodeNumberReservation.apply {
                    text = reservation.zip
                    visibility = View.VISIBLE
                }
                cancelCurrentReservationTV.visibility = View.VISIBLE
            }
        }
    }

    private fun showTooFarModal() {
        MPAlertDialog(context)
                .setMessage("You must be 100 yards from the theater to reveal your code.")
                .setPositiveButton(R.string.ok, null)
                .show()
    }

    fun showSnackBar() {

    }

    private fun determineIfCanReveal(onReveal: OnReveal) {
        if (canReveal) {
            return onReveal.onReveal()
        }
        locationManager?.location()
                ?.subscribe { t1, t2 ->
                    t1?.let {
                        if (canReveal) {
                            onReveal.onReveal()
                        } else {
                            onReveal.onTooFarAway()
                        }
                    }
                    t2?.let {
                        showSnackBar()
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

interface OnReveal {
    fun onReveal()
    fun onTooFarAway()
}

interface OnCloseListener {
    fun onClose()
    fun cancelReservation(reservationId: Int)
    fun openTicketVerificationFragment()
}