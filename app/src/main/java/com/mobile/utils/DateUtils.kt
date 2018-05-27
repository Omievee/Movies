package com.mobile.utils

import java.text.SimpleDateFormat
import java.util.*

fun isValidShowtime(timeStr: String?): Boolean {
    timeStr ?: return false
    val systemClock = Date()
    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
    val curTime = sdf.format(systemClock);
    val theaterTime = sdf.parse(timeStr);
    val myTime = sdf.parse(curTime)
    val cal = Calendar.getInstance().apply {
        time = theaterTime
        add(Calendar.MINUTE, 30)
    }
    return !myTime.after(cal.time)
}