package com.mobile

class ApiError(
        val error: Error=Error(),
        val httpErrorCode:Int=-1) : Throwable()