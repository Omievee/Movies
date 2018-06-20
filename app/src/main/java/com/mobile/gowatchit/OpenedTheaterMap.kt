package com.mobile.gowatchit

import com.google.gson.annotations.SerializedName

class OpenedTheaterMap(
        @SerializedName("ct")
        val clickThrough:String="Unset",
        @SerializedName("ci")
        val clearIt:String = "-1",
        @SerializedName("u")
        var url:String = "https://moviepass.com/go/map"
        ) : Base()