package com.mobile.theater

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.mobile.utils.MapUtil
import com.mobile.utils.startIntentIfResolves
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_theater.view.*

class TheaterItemView(context: Context, attr: AttributeSet? = null) : FrameLayout(context, attr) {

    var clickListener: TheaterClickListener? = null
    var presentation:TheaterPresentation? = null

    init {
        View.inflate(context, R.layout.list_item_theater, this)
        setOnClickListener {
            var theater = presentation?.theater?: return@setOnClickListener
            clickListener?.onTheaterClicked(theater)
        }

        distanceView.setOnClickListener {
            val screeningPresentation = this.presentation ?: return@setOnClickListener
            context.startIntentIfResolves(MapUtil
                    .mapIntent(
                            screeningPresentation
                                    .theater
                                    .lat,
                            screeningPresentation
                                    .theater
                                    .lon))
        }

    }

    fun bind(presentation: TheaterPresentation, listener: TheaterClickListener? = null) {
        this.presentation = presentation
        this.clickListener = listener
        theaterName.text = presentation.theater.name
        theaterAddress.text = presentation.theater.address
        val theater = presentation.theater
        theaterCity.text = "${theater.city}, ${theater.state} ${theater.zip}"
        theaterDistance.text =
                when(presentation.distance) {
                    null -> null
                    else-> "${presentation.distance} mi"
                }
        iconSeat.visibility = when (presentation.theater.ticketTypeIsSelectSeating()) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
        iconTicket.visibility = when (presentation.theater.ticketTypeIsETicket() || presentation.theater.ticketTypeIsSelectSeating()) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }
}