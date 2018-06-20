package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName
import com.mobile.UserPreferences
import com.moviepass.BuildConfig

open class GoWatchit(
        @SerializedName("m")
    val medium:String = "app",
        @SerializedName("mc")
    val mediumContext:String = "android",
        @Transient
    val campaign:String? = null,
        @SerializedName("o")
    val organic:String = "organic",
        @SerializedName("eid[movie_pass]")
    val userId:Int = UserPreferences.userId,
        @SerializedName("eid[idfa]")
    val idfa:String = UserPreferences.deviceAndroidID,
        @SerializedName("vn")
    val versionName:String = BuildConfig.VERSION_NAME,
        @SerializedName("vc")
    val versionCode:Int= BuildConfig.VERSION_CODE,
        @SerializedName("lts")
    val timestamp:Long = System.currentTimeMillis()) {
    @SerializedName("c")
    var _campaign:String? = null
    get() {
        return campaign?:"no_campaign"
    }

}