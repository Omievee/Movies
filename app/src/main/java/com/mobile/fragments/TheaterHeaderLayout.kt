package com.mobile.fragments

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.model.Theater
import com.mobile.utils.MapUtil
import com.mobile.utils.expandTouchArea
import com.mobile.utils.startIntentIfResolves
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_theater_header.view.*

class TheaterHeaderLayout(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var theater: Theater? = null

    init {
        View.inflate(context, R.layout.layout_theater_header, this)
        backButton
                .setOnClickListener {
                    val activity = context as? Activity ?: return@setOnClickListener
                    activity.onBackPressed()
                }
        clipChildren = false
        clipToPadding = false
        arrayOf(theaterStreet, theaterCity, theaterPin).forEach { view ->
            view.setOnClickListener {
                it.expandTouchArea()
                val theater = theater ?: return@setOnClickListener
                context.startIntentIfResolves(MapUtil.mapIntent(theater.lat, theater.lon))
            }
        }
    }

    fun bind(theater: Theater) {
        this.theater = theater
        theaterName.text = theater.name
        theaterStreet.text = theater.address
        theaterCity.text = theater.cityStateZip

    }
}