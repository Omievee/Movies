package com.mobile.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mobile.Constants
import com.mobile.model.Reservation
import kotlinx.android.parcel.Parcelize

@Parcelize
class ReservationResponse(var status: String? = null,
                          var zipCode: String? = null,
                          var showtime: String? = null,
                          val reservation: Reservation,
                          @SerializedName("e_ticket_confirmation") var eTicketConfirmation: ETicketConfirmation? = null
) : Parcelable {

    val isOk: Boolean
        get() = status!!.matches(Constants.API_RESPONSE_OK.toRegex())


}

@Parcelize
class ETicketConfirmation(
        var barCodeUrl: String? = null,
        var confirmationCode: String? = null,
        var confirmationCodeFormat: String? = null) : Parcelable {
}
