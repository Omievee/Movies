package com.mobile.adapters

import com.moviepass.R
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.model.TicketType
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.screening.ScreeningPresentation
import com.mobile.screening.ShowtimeAdapter
import com.mobile.utils.text.toFixed
import com.mobile.utils.text.toMiles

import kotlinx.android.synthetic.main.list_item_theaters_and_showtimes.view.*

class MovieScreeningView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    val adapter: ShowtimeAdapter = ShowtimeAdapter()
    var screening: ScreeningPresentation? = null
    var showtimeListener: ShowtimeClickListener? = null

    init {
        View.inflate(context, R.layout.list_item_theaters_and_showtimes, this)
        recyclerView.layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        }
        recyclerView.adapter = adapter
        val animator = DefaultItemAnimator()
        animator.supportsChangeAnimations = false
        recyclerView.itemAnimator = animator
        recyclerView.addItemDecoration(SpaceDecorator(resources.getDimension(R.dimen.card_button_margin_start).toInt()))
        val margin = resources.getDimension(R.dimen.margin_half).toInt()
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginStart = margin
            marginEnd = margin
            bottomMargin = margin * 2
        }
    }

    fun bind(p: ScreeningPresentation, showtimeClickListener: ShowtimeClickListener?) {
        this.screening = p
        this.showtimeListener = showtimeClickListener
        THEATER_NAME_LISTITEM.text = p.theater?.name
        THEATER_ADDRESS_LISTITEM.text = p.theater?.cityStateZip
        THEATER_ADDRESS2_LISTITEM.text = p.theater?.address
        THEATER_DISTANCE_LISTITEM.text = "${p.distance?.toMiles()?.toFixed(1)?.toString()} mi"
        icon_seat.visibility = when (screening?.screening?.getTicketType()) {
            TicketType.SELECT_SEATING -> View.VISIBLE
            else -> View.GONE
        }
        icon_ticket.visibility = when (screening?.screening?.getTicketType()) {
            TicketType.SELECT_SEATING, TicketType.E_TICKET -> View.VISIBLE
            else -> View.GONE
        }
        notSupported.visibility = when (screening?.screening?.approved) {
            true -> View.GONE
            else -> View.VISIBLE
        }
        notSupported.text = when {
            p.movie != null -> resources.getString(R.string.screening_already_seen)
            else-> p.screening?.disabledExplanation
        }
        movieApproved.isEnabled = screening?.enabled ?: false
        adapter.screening = screening
        adapter.showtimeClickListener = showtimeClickListener
        adapter.data = ShowtimeAdapter.createData(adapter.data, p)
    }
}