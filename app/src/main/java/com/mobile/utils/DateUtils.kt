package com.mobile.utils

import java.util.*
import java.util.Calendar.*
import java.text.SimpleDateFormat

class DateUtils {

    companion object {
        fun isSameDay(item: Date): Boolean {
            val itemCal = getInstance()
            itemCal.timeInMillis = item.time

            val now = getInstance()
            val dayOfYear = itemCal.get(DAY_OF_YEAR)
            val todaysDayOfYear = now.get(DAY_OF_YEAR)
            return dayOfYear == todaysDayOfYear
        }
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