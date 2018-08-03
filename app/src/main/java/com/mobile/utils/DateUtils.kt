package com.mobile.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.getInstance

class DateUtils {

    companion object {
        fun everyFourHours(item: Date): Boolean {
            val itemCal = getInstance()
            itemCal.timeInMillis = item.time

            val now = getInstance()
            val dayOfYear = itemCal.get(HOUR_OF_DAY)
            val todaysDayOfYear = now.get(HOUR_OF_DAY)
            return dayOfYear == todaysDayOfYear
        }
    }
}

val String.timeToCalendar:Calendar
get() {
    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
    val theaterTime = sdf.parse(this)
    return Calendar.getInstance().apply {
        time = theaterTime
    }
}

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