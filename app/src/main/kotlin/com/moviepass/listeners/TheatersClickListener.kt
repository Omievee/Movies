package com.moviepass.listeners

import com.moviepass.model.Theater

/**
 * Created by ryan on 4/27/17.
 */

interface TheatersClickListener {

    fun onTheaterClick(pos: Int, theater: Theater, posX: Int, posY: Int)

}