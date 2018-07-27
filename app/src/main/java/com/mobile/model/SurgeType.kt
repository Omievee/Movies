package com.mobile.model

enum class SurgeType(val level:Int, val description:String) {
    SURGING(2,"peak"),
    WILL_SURGE(1, "pre-peak"),
    NO_SURGE(0, "no peak")
}