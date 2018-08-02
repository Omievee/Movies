package com.mobile.surge

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.UserPreferences
import com.mobile.reservation.Checkin
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_guest_ticket.view.*

class SurgeTicketView(context: Context, attributeSet: AttributeSet?=null) : ConstraintLayout(context,attributeSet) {

    var infoClickListener:InfoClickListener? = null

    init {
        inflate(context, R.layout.layout_guest_ticket, this)
        infoIcon.visibility = View.VISIBLE
        infoIcon.setOnClickListener {
            infoClickListener?.onClickInfo()
        }
    }

    fun bind(checkin:Checkin, infoClickListener: InfoClickListener?=null) {
        val surge = checkin.screening.getSurge(checkin.availability.startTime,UserPreferences.restrictions.userSegments)
        val peakPass = checkin.peakPass
        ticketType.setText(R.string.peak_surcharge)
        this.infoClickListener = infoClickListener
        price.text = surge.costAsDollars
        val one = 1
        quantity.text = one.toString()
        total.text = surge.costAsDollars
        when(peakPass) {
            null-> {
                strikethrough.visibility=View.INVISIBLE
            }
            else-> {
                strikethrough.visibility=View.VISIBLE
            }
        }
    }
}

interface InfoClickListener {
    fun onClickInfo()
}
