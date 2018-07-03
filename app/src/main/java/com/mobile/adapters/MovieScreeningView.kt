package com.mobile.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mobile.listeners.ShowtimeClickListener
import com.mobile.model.TicketType
import com.mobile.recycler.decorator.SpaceDecorator
import com.mobile.screening.ScreeningPresentation
import com.mobile.screening.ShowtimeAdapter
import com.mobile.utils.text.toFixed
import com.mobile.utils.text.toMiles
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_theaters_and_showtimes.view.*

class MovieScreeningView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    val adapter: ShowtimeAdapter = ShowtimeAdapter()
    var screeningPresentation: ScreeningPresentation? = null
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
        recyclerView.addItemDecoration(SpaceDecorator(bottom = resources.getDimension(R.dimen.card_button_margin_start).toInt()))
        val margin = resources.getDimension(R.dimen.margin_half).toInt()
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginStart = margin
            marginEnd = margin
            bottomMargin = margin * 2
        }

        pinIcon.setOnClickListener {
            val screeningPresentation = this.screeningPresentation ?: return@setOnClickListener
            val uri = Uri.parse("geo:" + screeningPresentation.theater?.latitude + "," + screeningPresentation.theater?.longitude + "?q=" + Uri.encode(screeningPresentation.theater?.name))

            try {
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()))
                mapIntent.setPackage("com.google.android.apps.maps")
                context?.startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Google Maps isn't installed", Toast.LENGTH_SHORT).show()
            } catch (x: Exception) {
                x.message
            }

        }

    }

    fun bind(p: ScreeningPresentation, showtimeClickListener: ShowtimeClickListener?) {
        this.screeningPresentation = p
        this.showtimeListener = showtimeClickListener
        THEATER_NAME_LISTITEM.text = p.theater?.name
        THEATER_ADDRESS2_LISTITEM.text = p.theater?.address
        THEATER_DISTANCE_LISTITEM.text = "${p.distance?.toMiles()?.toFixed(1)?.toString()} mi"
        THEATER_ADDRESS_LISTITEM.text = p.theater?.cityStateZip
        icon_seat.visibility = when (screeningPresentation?.screening?.getTicketType()) {
            TicketType.SELECT_SEATING -> View.VISIBLE
            else -> View.GONE
        }
        icon_ticket.visibility = when (screeningPresentation?.screening?.getTicketType()) {
            TicketType.SELECT_SEATING, TicketType.E_TICKET -> View.VISIBLE
            else -> View.GONE
        }


        val disabledEx = screeningPresentation?.screening?.disabledExplanation ?: ""
        val approval = screeningPresentation?.screening?.approved ?: true

        notSupported.visibility =
                when (!approval || p.movie != null) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }

        notSupported.text = when {p.movie != null ->
            resources.getString(R.string.screening_already_seen)
            else -> screeningPresentation?.screening?.disabledExplanation
        }

        if (disabledEx.isEmpty() && !approval) {
            notSupported.text = resources.getString(R.string.screening_premium)
        }

        movieApproved.isEnabled = screeningPresentation?.enabled ?: false
        adapter.screening = screeningPresentation
        adapter.showtimeClickListener = showtimeClickListener
        adapter.data = ShowtimeAdapter.createData(adapter.data, p)
    }
}