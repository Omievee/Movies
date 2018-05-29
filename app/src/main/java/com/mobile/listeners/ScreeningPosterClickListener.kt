package com.mobile.listeners

import android.widget.ImageView

import com.mobile.model.Screening

/**
 * Created by anubis on 6/10/17.
 */

interface ScreeningPosterClickListener {

    fun onScreeningPosterClick(pos: Int, screening: Screening, startTimes: List<String>, shareImageView: ImageView)

}
