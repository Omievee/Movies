package com.moviepass.listeners

import com.moviepass.model.Screening

/**
 * Created by anubis on 6/13/17.
 */

interface ShowtimeClickListener {

    fun onShowtimeClick(pos: Int, screening: Screening, showtime: String)

}
