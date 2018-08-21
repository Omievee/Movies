package com.mobile.rx

import com.mobile.ApiError
import com.mobile.Error

class PendingChargesError(error: Error, httpErrorCode: Int) : ApiError(error, httpErrorCode) {
}