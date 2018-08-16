package com.mobile.model

class CappedPlan(var type:CapType=CapType.SOFT, var remaining: Int = 0, val used: Int = 0, var discount:Int=0) {
    val isOverSoftCap: Boolean
        get() {
            return remaining==0 && type==CapType.SOFT
        }
    val isOverHardCap:Boolean
    get() {
        return remaining==0 && type==CapType.HARD
    }
    val asDollars:Double
    get() {
        return discount.div(100.0)
    }
}

enum class CapType {
    SOFT, HARD
}