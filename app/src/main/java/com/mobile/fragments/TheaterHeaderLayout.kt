package com.mobile.fragments

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.model.Theater
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_theater_header.view.*

class TheaterHeaderLayout(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.layout_theater_header, this)
        backButton
                .setOnClickListener {
                    val activity = context as? Activity ?: return@setOnClickListener
                    activity.onBackPressed()
                }
        clipChildren = false
        clipToPadding = false
    }

    fun bind(theater: Theater) {
        theaterName.text = theater.name
        theaterStreet.text = theater.address
        theaterCity.text = theater.cityStateZip

    }
}