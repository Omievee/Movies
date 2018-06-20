package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName

class OpenedTheaterList(
        @SerializedName("ct")
        val clickThrough:String="Unset",
        @SerializedName("ci")
        val clearIt:String = "-1"
) : Base() {

    @SerializedName("u")
    var url:String? = null
        get() {
            return "https://moviepass.com/go/list"
        }
}