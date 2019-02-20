package com.mobile.listeners

import com.mobile.model.Screening
import com.mobile.model.Theater

/**
 * Created by anubis on 6/13/17.
 */

interface ShowtimeClickListener {
    fun onShowtimeClick(theater: Theater?, screening: Screening, showtime: String)
}
