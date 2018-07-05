package com.mobile.network

import com.google.gson.annotations.SerializedName

data class SurgeResponse(
        @SerializedName("data.attributes.currentlyPeaking")
        val currentlyPeaking:Boolean=false,
        @SerializedName("data.attributes.peakMessage")
        val peakMessage:String?=null,
        @SerializedName("data.attributes.peakAmount")
        val peakAmount:Int=0
)
