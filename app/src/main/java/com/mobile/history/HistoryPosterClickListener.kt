package com.mobile.history

import com.mobile.history.model.ReservationHistory

/**
 * Created by o_vicarra on 3/27/18.
 */

interface HistoryPosterClickListener {

    fun onPosterClicked(pos: Int, historyposter: ReservationHistory, isRatingScreen: Boolean)
}