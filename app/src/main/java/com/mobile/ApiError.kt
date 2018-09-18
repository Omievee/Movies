package com.mobile

open class ApiError(
        val error: Error=Error(),
        val httpErrorCode:Int=-1) : Throwable()