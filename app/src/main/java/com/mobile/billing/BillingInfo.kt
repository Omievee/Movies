package com.mobile.billing

class BillingInfo(
        val section: String? = "creditCardData",
        var paymentInfo: PaymentInfo? = null,
        var billingAddress: BillingAddress? = null
)

class PaymentInfo(
        var number: String? = null,
        var cvv: String? = null,
        var expirationDate: String? = null
)

class BillingAddress(
        var street: String? = null,
        var street2: String? = null,
        var city: String? = null,
        var state: String? = null,
        var zip: String? = null
)