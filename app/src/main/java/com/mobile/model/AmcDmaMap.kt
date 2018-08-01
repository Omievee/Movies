package com.mobile.model

class AmcDmaMap : HashMap<String, Boolean>() {
    fun shouldMoveToBottom(theater: Theater?): Boolean {
        theater?:return false
        return containsKey(theater.zip) && theater.theaterChainName == "AMC Theatres"
    }

    fun hasOneMoveToBottom(theaters:List<Theater>):Boolean {
        return theaters.any { shouldMoveToBottom(it) }
    }
}