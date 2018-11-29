package com.mobile.history.response

import com.google.gson.annotations.SerializedName
import com.mobile.history.model.ReservationHistory

class ReservationHistoryResponse(
        @SerializedName("data")
        var reservations: List<ReservationHistory>? = null)