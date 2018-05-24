package com.mobile.responses

import com.mobile.model.UserInfo


/**
 * Created by anubis on 8/1/17.
 */

class UserInfoResponse(

        var authToken: String? = null,
        var user: UserInfo? = null,
        var billingAddressLine1: String? = null,
        var billingAddressLine2: String? = null,
        var billingCard: String? = null,
        var nextBillingDate: String? = null,
        var shippingAddressLine1: String? = null,
        var shippingAddressLine2: String? = null,
        var plan: String? = null,
        var moviePassCardNumber: String? = null,
        var remainingCap: String? = null,
        val email: String? = null
) {

    val name: String
        get() = user?.firstName ?: ""+" "+user?.lastName ?: ""

    val zipCode:String? by lazy {
        shippingAddressLine2?.split(",")?.let {
            when(it.size>0) {
                true-> it.get(it.size-1).trim()
                else-> ""
            }
        }
    }
}
