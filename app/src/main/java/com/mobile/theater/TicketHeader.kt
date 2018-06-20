package com.mobile.theater

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_ticket_header.view.*

class TicketHeader(context: Context?) : ConstraintLayout(context) {

    init {
        inflate(context, R.layout.layout_ticket_header, this)
        layoutParams = MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun bind(id:Int) {
        text.setText(id)
    }

}