package com.mobile.listeners

import com.mobile.model.Screening

/**
 * Created by ryan on 4/27/17.
 */

interface MovieTheaterClickListener {

    fun onTheaterClick(pos: Int, screening: Screening)
}
