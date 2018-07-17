package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName

class AppOpened : Base() {
    @SerializedName("ct")
    val clickThrough = "Unset"
    @SerializedName("ci")
    val clearit = -1
    @SerializedName("e")
    override var event = "app_open"
}