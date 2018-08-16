package com.mobile.network

data class RestrictionsCheckResponse(val data: RestrictionsData = RestrictionsData())

class RestrictionsData(
        val type: RestrictionsCheckType = RestrictionsCheckType.NO_RESTRICTIONS,
        val attributes: RestrictionsAttributes? = RestrictionsAttributes()
) {
    val overSoftCap: Boolean
        get() {
            return type == RestrictionsCheckType.CAP && attributes?.soft==true
        }

    val overHardCap: Boolean
        get() {
            return type == RestrictionsCheckType.CAP && attributes?.soft != true
        }
}

class RestrictionsAttributes(
        val peakMessage: String? = null,
        val peakAmount: Int = 0,
        val soft: Boolean = true,
        val title: String? = null,
        val message: String? = null,
        val discount: Int = 0,
        val currentlyPeaking: Boolean = false)

enum class RestrictionsCheckType {
    CAP,
    PEAK,
    NO_RESTRICTIONS
}