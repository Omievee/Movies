package com.mobile.listeners

import com.mobile.model.Theater

/**
 * Created by ryan on 4/27/17.
 */

interface TheatersClickListener {

    fun onTheaterClick(pos: Int, theater: Theater)

}