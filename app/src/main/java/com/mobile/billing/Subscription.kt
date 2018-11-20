package com.mobile.billing

import com.mobile.model.ParcelableDate

class Subscription(val data: SubscriptionData)

class SubscriptionData(
        val plan: Plan = Plan(),
        val billingInfo: BillingInfo = BillingInfo(),
        val paidThroughDate: ParcelableDate? = null
)

class Plan(
        val id: String = "",
        val planType: String = "",
        val name: String = "",
        val lengthMonths: Int = 0,
        val signUpFee: Int = 0,
        val installmentAmount: Int = 0,
        val cap: Int = 0
)

class BillingInfo(
        var billingAddress: BillingAddress? = null,
        var creditCardInfo: CreditCardInfo? = null
)

class BillingAddress(
        var firstName: String? = null,
        var lastName: String? = null,
        var address1: String? = null,
        var address2: String? = null,
        var city: String? = null,
        var state: String? = null,
        var postalCode: String? = null
)

class CreditCardInfo(
        var cardNumber: String? = null,
        var expirationMonth: String? = null,
        var expirationYear: String? = null,
        var securityCode: String? = null)