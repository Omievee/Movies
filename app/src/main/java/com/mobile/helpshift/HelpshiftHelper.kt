package com.mobile.helpshift

import android.app.Activity
import android.util.Base64
import com.helpshift.HelpshiftUser
import com.helpshift.support.ApiConfig
import com.helpshift.support.Metadata
import com.helpshift.support.Support
import com.mobile.UserPreferences
import com.moviepass.BuildConfig
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HelpshiftHelper {

    companion object {

        fun getHelpshiftUser(): HelpshiftUser {
            val helpshiftUser: HelpshiftUser.Builder = HelpshiftUser.Builder(UserPreferences.userId.toString(),
                    UserPreferences.userEmail).setName(UserPreferences.firstName)
            return helpshiftUser.build()
        }

        fun startHelpshiftConversation(activity: Activity) {
            val customIssueFileds = HashMap<String, Array<String>>()
            val userData = HashMap<String, Any>()
            customIssueFileds["version name"] = arrayOf("sl", BuildConfig.VERSION_NAME)

            val token = UserPreferences.lastReservation
            val checkinAttempt = UserPreferences.lastCheckInAttempt
            val checkedIn: Boolean

            if (token != null && token.reservation.reservation!=null) {
                val rs = token.reservation.reservation
                checkedIn = rs.expiration > System.currentTimeMillis()
                val starttime = token.getTimeAsDate()
                val diff = starttime?.getTime()?:0 - System.currentTimeMillis()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
                if (checkedIn && minutes >= -30) {
                    customIssueFileds["minutes_until_showtime"] = arrayOf("n", minutes.toString())
                }
            } else {
                checkedIn = false
            }

            if (checkinAttempt != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val diff = System.currentTimeMillis() - checkinAttempt.time.getTime()
                val diffHours = diff / (60 * 60 * 1000)
                val diffMinutes = diff / (60 * 1000)

                customIssueFileds["last_check_in_attempt_date"] = arrayOf("sl", dateFormat.format(checkinAttempt.time))
                customIssueFileds["last_check_in_attempt_time"] = arrayOf("sl", timeFormat.format(checkinAttempt.time))
                customIssueFileds["hours_since_last_checkin_attempt"] = arrayOf("n", diffHours.toString())
                customIssueFileds["minutes_since_last_checkin_attempt"] = arrayOf("n", diffMinutes.toString())

                val screening = checkinAttempt.screening
                val availability = checkinAttempt.availability
                val surge = screening.getSurge(availability.startTime, UserPreferences.restrictions.userSegments)
                customIssueFileds["peak_level"] = arrayOf("dd", surge.level.level.toString() + " - " + surge.level.description)
                customIssueFileds["peak_fee"] = arrayOf("n", surge.amount.toString())
            }

            customIssueFileds["subscription_type"] = arrayOf("dd", UserPreferences.restrictions.subscriptionStatus.name)
            customIssueFileds["checked_in"] = arrayOf("b", checkedIn.toString())
            customIssueFileds["total_movies_seen"] = arrayOf("n", UserPreferences.totalMovieSeen.toString())
            customIssueFileds["total_movies_seen_last_thirty_days"] = arrayOf("n", UserPreferences.totalMovieSeenLastMonth.toString())
            userData["last_movie_seen"] = UserPreferences.lastMovieSeen?:""
            val tags = arrayOf<String>(BuildConfig.VERSION_NAME)

            userData["version"] = BuildConfig.VERSION_NAME

            val meta = Metadata(userData, tags)

            val apiConfig = ApiConfig.Builder()
                    .setEnableContactUs(Support.EnableContactUs.ALWAYS)
                    .setCustomIssueFields(customIssueFileds)
                    .setCustomMetadata(meta)
                    .build()

            Support.showFAQs(activity, apiConfig)
        }

        private fun sign(helpshiftUser: HelpshiftUser, secretKey:String): String? {
            return try {
                val hmacsha256 = Mac.getInstance("HmacSHA256")
                hmacsha256.init(SecretKeySpec(secretKey.toByteArray(), "HmacSHA256"));
                val msg = arrayOf(helpshiftUser.identifier ?: "", helpshiftUser.email
                        ?: "").joinToString()
                val hash = hmacsha256.doFinal(msg.toByteArray());
                Base64.encodeToString(hash, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }
}