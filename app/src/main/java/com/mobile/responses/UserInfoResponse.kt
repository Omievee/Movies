package com.mobile.responses

import com.mobile.model.ParcelableDate
import com.mobile.model.User


/**
 * Created by anubis on 8/1/17.
 */

data class UserInfoResponse(

        var authToken: String? = null,
        var user: User? = null,
        var billingAddressLine1: String? = null,
        var billingAddressLine2: String? = null,
        var billingCard: String? = null,
        var nextBillingDate: ParcelableDate? = null,
        var shippingAddressLine1: String? = null,
        var shippingAddressLine2: String? = null,
        var plan: String? = null,
        var moviePassCardNumber: String? = null,
        var remainingCap: String? = null,
        val email: String? = null
) {

}