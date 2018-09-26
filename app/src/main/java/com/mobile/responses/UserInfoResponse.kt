package com.mobile.responses

import com.mobile.billing.BillingAddress
import com.mobile.billing.BillingInfo
import com.mobile.billing.PaymentInfo
import com.mobile.model.ParcelableDate
import com.mobile.model.User


/**
 * Created by anubis on 8/1/17.
 */

class UserInfoResponse(

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

    val billingZipCode:String? by lazy {
        billingAddressLine2?.split(",")?.let {
            when(it.size>0) {
                true-> it.get(it.size-1).trim()
                else-> ""
            }
        }
    }

    companion object {

        fun getBillingInfo(user: UserInfoResponse) : BillingInfo{
            val billingAddress = user.billingAddressLine2
            val billingAddressList = billingAddress?.split(",".toRegex(),0)
            var city : String? = null
            var state: String? = null
            var zip: String? =  null

            if(billingAddressList?.size ?: 0 >= 3){
                city = billingAddressList?.get(0)?.trim()
                state = billingAddressList?.get(1)?.trim()
                zip = billingAddressList?.get(2)?.trim()
            }

            val creditNumber = user.billingCard
            var expiration : String? = null
            var CVV : String? = null
            if(!creditNumber.isNullOrEmpty()){
                expiration = "##/####"
                CVV  = "###"
            }

            return BillingInfo(null,
                    PaymentInfo(
                            creditNumber,
                            CVV,
                            expiration
                    ),
                    BillingAddress(
                            user.billingAddressLine1,
                            null,
                            city,
                            state,
                            zip
                    ))
        }
    }
}