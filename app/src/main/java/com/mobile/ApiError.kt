package com.mobile

class ApiError(
        val error: Error?=null,
        val httpErrorCode:Int=-1) : Throwable()