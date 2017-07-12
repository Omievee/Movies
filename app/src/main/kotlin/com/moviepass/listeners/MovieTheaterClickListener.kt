package com.moviepass.listeners

import com.moviepass.model.Screening

/**
 * Created by ryan on 4/27/17.
 */

interface MovieTheaterClickListener {

    fun onTheaterClick(pos: Int, screening: Screening)
}
