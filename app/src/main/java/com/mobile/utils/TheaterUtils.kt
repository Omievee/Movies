package com.mobile.utils

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.mobile.Constants
import com.mobile.fragments.MPFragment
import com.mobile.fragments.TheaterPolicy
import com.mobile.model.Movie
import com.mobile.model.Theater
import com.moviepass.R
import java.text.SimpleDateFormat
import java.util.*

val Theater.isFlixBrewhouse: Boolean
    get() {
        return this.name?.toLowerCase()?.contains("flix brewhouse") == true
    }

fun Fragment.showTheaterBottomSheetIfNecessary(theater: Theater?) {
    when (theater?.isFlixBrewhouse) {
        true -> {
            val bundle = Bundle()
            bundle.putString(Constants.POLICY, theater.name)

            val fragobj = TheaterPolicy()
            fragobj.arguments = bundle
            fragobj.show(childFragmentManager, "fr_theaterpolicy")
        }
    }
}

val Movie.releaseDateFormatted: String?
    get() {
        val date = releaseDateTime ?: return null
        return SimpleDateFormat("MMMM d, yyyy").format(date)
    }

val Movie.isComingSoon: Boolean
    get() {
        return try {
            val date: Calendar = releaseDateTime?.calendar ?: return false
            date.clearTime()
            date.after(Calendar.getInstance())
        } catch (e: Error) {
            false
        }
    }
val Movie.releaseDateTime: Date?
    get() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s")
        val date = releaseDate ?: return null
        return try {
            sdf.parse(date)
        } catch (e: Error) {
            null
        }
    }

val Date.calendar: Calendar
    get() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = this.time
        return cal
    }

fun Calendar.clearTime() {
    this.set(Calendar.HOUR_OF_DAY, 0)
    this.set(Calendar.MINUTE, 0)
    this.set(Calendar.SECOND, 0)
    this.set(Calendar.MILLISECOND, 0)
}