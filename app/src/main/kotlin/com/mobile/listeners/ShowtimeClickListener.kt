package com.mobile.listeners

import com.mobile.model.Screening

/**
 * Created by anubis on 6/13/17.
 */

interface ShowtimeClickListener {

    fun onShowtimeClick(pos: Int, screening: Screening, showtime: String)

}
