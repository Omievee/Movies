package com.mobile.seats

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.utils.MapUtil.Companion.mapIntent
import com.mobile.utils.startIntentIfResolves
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_movie_screening_poster_header.view.*

class MovieScreeningPosterHeader(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {
    init {
        inflate(context, R.layout.layout_movie_screening_poster_header, this)
    }

    @SuppressLint("SetTextI18n")
    fun bind(payload: SelectSeatPayload) {
        val checkin = payload.checkin?:return
        moviePoster.setImageURI(checkin.screening.imageUrl)
        movieTitle.text = checkin.screening.title
        theaterName.text = checkin.theater.name ?: checkin.screening.theaterName
        arrayOf(theaterName, theaterPin).forEach {
            it.setOnClickListener {
                val lat = checkin.theater.lat
                val lng = checkin.theater.lon
                context.startIntentIfResolves(mapIntent(lat, lng))
            }
        }
        showTime.text = checkin.availability.startTime
        val seatsSize = payload.selectedSeats?.size ?: 1
        val seatText = resources.getQuantityString(R.plurals.seats, seatsSize, seatsSize)
        seats.text = "${seatText} ${payload.selectedSeats?.joinToString(", ") {
            it.seatName ?: ""
        } ?: ""}"
        seats.visibility = when (payload.selectedSeats?.size ?: 0 > 0) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}