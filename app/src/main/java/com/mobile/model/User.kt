package com.mobile.model

data class User(
        var id: Int = 0,
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var authToken: String? = null,
        var password: String? = null,
        var oneDeviceId: String? = null,
        var androidID: String? = null
)
