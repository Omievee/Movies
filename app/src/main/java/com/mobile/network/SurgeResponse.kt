package com.mobile.network

data class SurgeResponse(val data: SurgeData = SurgeData()) {
    val peakMessage: String?
        get() {
            return data.attributes.peakMessage
        }
    val peakAmount: Int
        get() {
            return data.attributes.peakAmount
        }
    val currentlyPeaking: Boolean
        get() {
            return data.attributes.currentlyPeaking
        }
}

class SurgeData(val attributes: SurgeAttributes = SurgeAttributes())

class SurgeAttributes(val peakMessage: String? = null, val peakAmount: Int = 0, val currentlyPeaking: Boolean = false)