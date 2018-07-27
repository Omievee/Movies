package com.mobile.theater

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.maps.model.Marker
import com.mobile.model.Theater
import com.mobile.utils.padding
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_theaters_infowindow.view.*

class CustomInfoWindow(context: Context?) : FrameLayout(context) {

    init {
        inflate(context, R.layout.fr_theaters_infowindow, this)
    }

    fun bind(theater: Marker, t: Theater) {
        infoTheaterName.text = theater.title

        infoAddress1.text = t.address
        infoAddress2.text = "${t.city}, ${t.state} ${t.zip}"

        infoEtix.visibility = when (t.ticketTypeIsETicket() || t.ticketTypeIsSelectSeating()) {
            true -> View.VISIBLE
            false -> {
                iconContainer.padding(bottom=0)
                View.GONE}
        }
        infoSeat.visibility = when (t.ticketTypeIsSelectSeating()) {
            true -> View.VISIBLE
            false -> View.GONE
        }
        reserveSeat.visibility = infoEtix.visibility
        iconContainer.visibility = infoEtix.visibility
    }
}