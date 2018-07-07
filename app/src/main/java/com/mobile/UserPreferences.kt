package com.mobile

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.gson.Gson

import com.mobile.history.model.ReservationHistory
import com.mobile.model.ScreeningToken
import com.mobile.responses.MicroServiceRestrictionsResponse
import com.mobile.responses.SubscriptionStatus
import com.mobile.responses.UserInfoResponse

import java.util.Calendar

/**
 * Created by ryan on 4/26/17.
 */

object UserPreferences {

    private lateinit var sPrefs: SharedPreferences
    private lateinit var gson:Gson

    var restrictionsLoaded:Boolean = false
    var restrictions:MicroServiceRestrictionsResponse = MicroServiceRestrictionsResponse()
    set(it) {
        field = it
        sPrefs.edit()
                .putString(Constants.RESTRICTIONS,
                       gson.toJson(it)).apply()
    }
    get() {
        if(restrictionsLoaded==false) {
            val ss = sPrefs.getString(Constants.RESTRICTIONS,null)
            if(ss==null) {
                field = MicroServiceRestrictionsResponse()
            } else {
                try {
                    field = gson.fromJson(ss, MicroServiceRestrictionsResponse::class.java)
                } catch (e:Exception) {
                    field = MicroServiceRestrictionsResponse()
                }
            }
            restrictionsLoaded = true
        }
        return field
    }

    val deviceAndroidID: String
        get() {
            return sPrefs.getString(Constants.DEVICE_ID, "ID") ?: "ID"
        }

    val hasUserLoggedInBefore: Boolean
        get() {
            return sPrefs.getBoolean(Constants.IS_USER_FIRST_LOGIN, false) ?: false
        }

    val hasUserSeenCardActivationScreen: Boolean
        get() {
            return sPrefs.getBoolean(Constants.CARD_ACTIVATED_SCREEN, false) ?: false
        }

    var oneDeviceId: String?
        get() {
            return sPrefs.getString(Constants.ONE_DEVICE_ID, null)
        }
        set(id) {
            val editor = sPrefs.edit()
            editor.putString(Constants.ONE_DEVICE_ID, id)
            editor.apply()
        }

    val userId: Int
        get() {
            return sPrefs.getInt(Constants.USER_ID, 0)
        }

    val deviceUuid: String
        get() {
            return sPrefs.getString(Constants.DEVICE_ANDROID_ID, "device") ?: "device"
        }

    val authToken: String
        get() {
            return sPrefs.getString(Constants.USER_AUTH_TOKEN, "") ?: ""
        }

    val userName: String
        get() {
            return sPrefs.getString(Constants.USER_FIRST_NAME, "bob") ?: "bob"
        }

    val userEmail: String
        get() {
            return sPrefs.getString(Constants.USER_EMAIL, "email") ?: "email"
        }

    val firebaseHelpshiftToken: String
        get() {
            return sPrefs.getString(Constants.FIREBASE_TOKEN, "null") ?: "null"
        }

    val lastCheckInAttemptDate: Long?
        get() {
            val dateKey = Constants.LAST_CHECK_IN_ATTEMPT_DATETIME + "_" + userId
            val `val` = sPrefs.getLong(dateKey, -1)
            return if (`val` == -1L) {
                null
            } else `val`
        }

    val location: Location
        get() {
            val location = Location("current")
            location.latitude = latitude
            location.longitude = longitude

            return location
        }

    val longitude: Double
        get() {
            return java.lang.Double.longBitsToDouble(sPrefs?.getLong(Constants.PREFS_LONGITUDE, java.lang.Double.doubleToLongBits(0.0))
                    ?: 0)
        }

    val latitude: Double
        get() {
            return java.lang.Double.longBitsToDouble(sPrefs?.getLong(Constants.PREFS_LATITUDE, java.lang.Double.doubleToLongBits(0.0))
                    ?: 0)
        }

    var pushPermission: Boolean
        get() {
            return sPrefs.getBoolean(Constants.PUSH_PERMISSION, true) ?: true
        }
        set(pushPermission) {
            val editor = sPrefs.edit()

            editor.putBoolean(Constants.PUSH_PERMISSION, pushPermission)
            editor.apply()
        }

    var alertDisplayedId: String?
        get() {
            return sPrefs.getString(Constants.ALERT_ID, "id") ?: "id"
        }
        set(alertID) {

            val edit = sPrefs.edit()
            edit.putString(Constants.ALERT_ID, alertID)
            edit.apply()

        }

    val lastReservationPopInfo: Int
        get() {
            return sPrefs?.getInt(Constants.RESERVATION_ID, 0) ?: 0
        }

    val billing: UserInfoResponse?
        get() {
            val key = Constants.BILLING + "_" + userId
            val billing = sPrefs.getString(key, null)
            if (billing != null) {
                try {
                    return gson.fromJson(billing, UserInfoResponse::class.java)
                } catch (ignored: Exception) {

                }

            }
            return null
        }

    var zipCode: String?
        get() {
            return sPrefs.getString(Constants.ZIP_CODE, null)
        }
        set(zip) = sPrefs.edit().putString(Constants.ZIP_CODE, zip).apply()

    val lastReservation: ScreeningToken?
        get() {
            val key = Constants.LAST_CHECK_IN_RESERVATION + "_" + userId
            val reservation = sPrefs.getString(key, null) ?: return null
            try {
                return gson.fromJson(reservation, ScreeningToken::class.java)
            } catch (ignored: Exception) {

            }
            return null
        }


    val isHistoryLoadedToday: Boolean
        get() {
            val cal = Calendar.getInstance()
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            return sPrefs.getInt(Constants.LAST_DOWNLOADED_HISTORY, -1) == dayOfYear
        }

    val aAID: String?
        get() {
            return sPrefs.getString(Constants.AAID, null)
        }

    val totalMovieSeen: Int
        get() {
            return sPrefs?.getInt(Constants.TOTAL_MOVIES_SEEN + "_" + userId, -1) ?: -1
        }

    val totalMovieSeenLastMonth: Int
        get() {
            return sPrefs?.getInt(Constants.TOTAL_MOVIES_SEEN_LAST_DAYS + "_" + userId, -1) ?: -1
        }

    val lastMovieSeen: String?
        get() {
            return sPrefs.getString(Constants.LAST_MOVIE_SEEN + "_" + userId, null)
        }

    fun load(context: Context, gson:Gson) {
        sPrefs = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE)
        this.gson = gson
    }

    fun saveDeviceAndroidID(deviceId: String?) {
        val editor = sPrefs.edit()
        editor.putString(Constants.DEVICE_ANDROID_ID, deviceId)
        editor.apply()
    }

    fun hasUserLoggedInBefore(isUserFirstLogin: Boolean) {
        val editor = sPrefs.edit()
        editor.putBoolean(Constants.IS_USER_FIRST_LOGIN, isUserFirstLogin)
        editor.apply()
    }


    fun setUserHasSeenCardActivationScreen(cardScreen: Boolean) {
        val editor = sPrefs.edit()
        editor.putBoolean(Constants.CARD_ACTIVATED_SCREEN, cardScreen)
        editor.apply()
    }

    fun verifyAndroidIDFirstRun(isAndroidIDVerified: Boolean) {
        val edit = sPrefs.edit()
        edit.putBoolean(Constants.IS_ANDROID_ID_VERIFIED, isAndroidIDVerified)
        edit.apply()
    }


    fun saveAAID(id: String) {
        val editor = sPrefs.edit()
        editor.putString(Constants.AAID, id)
        editor.apply()
    }

    fun updateEmail(email: String) {
        val editor = sPrefs.edit()
        editor.putString(Constants.USER_EMAIL, email)
        editor.apply()
    }


    fun setHeaders(authToken: String, user_id: Int) {
        val editor = sPrefs.edit()
        editor.putString(Constants.USER_AUTH_TOKEN, authToken)
        editor.putInt(Constants.USER_ID, user_id)
        editor.apply()
    }

    fun setUserCredentials(userId: Int, deviceAndroidID: String?, authToken: String?,
                           firstName: String?, email: String?, oneDeviceID: String?) {
        val editor = sPrefs.edit() ?: return

        editor.putInt(Constants.USER_ID, userId)
        editor.putString(Constants.DEVICE_ANDROID_ID, deviceAndroidID)
        editor.putString(Constants.USER_AUTH_TOKEN, authToken)
        editor.putString(Constants.USER_FIRST_NAME, firstName)
        if (oneDeviceID != null && !oneDeviceID.isEmpty())
            editor.putString(Constants.ONE_DEVICE_ID, oneDeviceID)
        editor.putString(Constants.USER_EMAIL, email)
        editor.apply()
    }

    fun clearUserId() {
        val editor = sPrefs.edit() ?: return

        editor.putInt(Constants.USER_ID, 0)
        editor.apply()
    }

    fun setLocation(lat: Double, lng: Double) {
        val editor = sPrefs.edit() ?: return
        editor.putLong(Constants.PREFS_LATITUDE, java.lang.Double.doubleToRawLongBits(lat))
        editor.putLong(Constants.PREFS_LONGITUDE, java.lang.Double.doubleToRawLongBits(lng))
        editor.apply()
    }

    fun saveFirebaseHelpshiftToken(refreshedToken: String) {
        val editor = sPrefs.edit() ?: return
        editor.putString(Constants.FIREBASE_TOKEN, refreshedToken).apply()
    }

    fun clearEverything() {
        val logIn = hasUserLoggedInBefore
        val editor = sPrefs.edit() ?: return
        editor.clear().apply()

        hasUserLoggedInBefore(logIn)
    }

    fun setLastCheckInAttemptDate() {
        val editor = sPrefs.edit() ?: return
        val dateKey = Constants.LAST_CHECK_IN_ATTEMPT_DATETIME + "_" + userId
        editor.putLong(dateKey, System.currentTimeMillis())
        editor.apply()
    }

    fun clearFbToken() {
        sPrefs.edit().putString(Constants.FB_TOKEN, "token").apply()
    }

    fun saveReservation(reservation: ScreeningToken?) {
        if (reservation != null) {
            val key = Constants.LAST_CHECK_IN_RESERVATION + "_" + userId
            val gson = gson.toJson(reservation)
            sPrefs.edit().putString(key, gson).apply()
        }
    }

    fun saveBilling(userPreferences: UserInfoResponse?) {
        if (userPreferences != null) {
            val key = Constants.BILLING + "_" + userId
            val gson = gson.toJson(userPreferences)
            sPrefs.edit().putString(key, gson).apply()
        }
    }

    fun saveLastReservationPopInfo(reservationId: Int) {
        sPrefs.edit().putInt(Constants.RESERVATION_ID, reservationId).apply()
    }

    fun saveTheatersLoadedDate() {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        sPrefs.edit().putInt(Constants.LAST_DOWNLOADED_THEATERS, dayOfYear).apply()
    }

    fun saveHistoryLoadedDate() {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        sPrefs.edit().putInt(Constants.LAST_DOWNLOADED_HISTORY, dayOfYear).apply()
    }

    fun setTotalMoviesSeen(totalMoviesSeen: Int) {
        sPrefs
                .edit().putInt(Constants.TOTAL_MOVIES_SEEN + "_" + userId, totalMoviesSeen).apply()
    }

    fun setTotalMoviesSeenLast30Days(totalMoviesSeenLast30Days: Int) {
        sPrefs.edit().putInt(Constants.TOTAL_MOVIES_SEEN_LAST_DAYS + "_" + userId, totalMoviesSeenLast30Days).apply()
    }

    fun setLastMovieSeen(movie: ReservationHistory) {
        sPrefs.edit().putString(Constants.LAST_MOVIE_SEEN + "_" + userId, movie.title).apply()
    }

    fun setShownPeakPricing() {
        val prefs = sPrefs.edit() ?: return
        prefs
                .putBoolean(Constants.SHOWN_PEAK_PRICING_ALERT, true).apply()
    }

    val shownPeakPricing:Boolean
    get() {
        return sPrefs.getBoolean(Constants.SHOWN_PEAK_PRICING_ALERT, false)
    }
}
