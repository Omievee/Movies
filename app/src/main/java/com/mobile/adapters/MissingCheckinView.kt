package com.mobile.adapters

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.mobile.model.Screening
import com.mobile.screening.ScreeningPresentation
import com.moviepass.R
import kotlinx.android.synthetic.main.list_item_cinemaposter.view.*

class MissingCheckinView(context: Context) : FrameLayout(context) {

    var missingCheckInListener: MissingCheckinListener? = null
    var screening: Screening? = null

    init {
        inflate(context, R.layout.list_item_cinemaposter, this)
        movieRating.visibility = View.GONE
        recyclerView.visibility = View.GONE
        synopsis.visibility = View.GONE
        setOnClickListener {
            screening?.let {
                missingCheckInListener?.onClick(it, screening?.startTimes?.get(0) ?: "")
            }
        }
        posterSPV.setImageResource(R.drawable.film_reel_wrapper)
        posterSPV.setBackgroundResource(R.drawable.missing_showtime_bg);
        movieTitle.setText(R.string.screening_unlisted_showtime)
    }

    fun bind(presenter: ScreeningPresentation, misssingCheckInListener: MissingCheckinListener?) {
        this.screening = presenter.screening
        this.missingCheckInListener = misssingCheckInListener
        posterSPV.isSelected = presenter.selected != null
        movieTime.text = presenter.screening?.synopsis
    }

}

interface MissingCheckinListener {
    fun onClick(screening: Screening, showTime: String)
}