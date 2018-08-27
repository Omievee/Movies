package com.mobile.screenings

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.mobile.model.Movie
import com.mobile.utils.isComingSoon
import com.mobile.utils.releaseDateFormatted
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_synopsis.view.*

class SynopsisView(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs, R.style.SynopsisFragment), PinToAppBar {
    init {
        View.inflate(context, R.layout.layout_synopsis, this)
        setBackgroundResource(R.drawable.dialog_fragment_graient)
    }

    fun bind(movie: Movie) {
        synopsisTitle.text = when {
            movie.isComingSoon -> resources.getString(R.string.in_theaters,movie.releaseDateFormatted)
            else -> movie.title
        }
        synopsisText.text = movie.synopsis
     }
}