package com.mobile.tv

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.mobile.fragments.TicketVerificationV2
import com.mobile.network.RestClient
import com.mobile.requests.VerificationLostRequest
import com.mobile.responses.VerificationLostResponse
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_ticket_verification_no_stub_v2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val RESERVATION_ID = "reservation_id"

class TicketVerificationNoStubV2 : MPFragment(), TicketVerificationNoStubView.SubmitListener {
    override fun submitNoStubMessage() {
        val lostTicket = VerificationLostRequest(ticketVerificationNoStubV.getReason())

        RestClient.getAuthenticated().lostTicket(reservationID, lostTicket).enqueue(object : Callback<VerificationLostResponse> {
            override fun onResponse(call: Call<VerificationLostResponse>, response: Response<VerificationLostResponse>) {
                val lostResponse = response.body()
                if (lostResponse != null) {
                    ticketVerificationNoStubV.displayWarning()
                    UserPreferences.saveLastReservationPopInfo(reservationID)
                }
            }

            override fun onFailure(call: Call<VerificationLostResponse>, t: Throwable) {
                Toast.makeText(context, "Error submitting ticket verification message",Toast.LENGTH_SHORT).show()
            }
        })
    }

    var reservationID: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reservationID = it.getInt(RESERVATION_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ticket_verification_no_stub_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ticketVerificationNoStubV.bind(this)
    }


    override fun closeFragment() {
        var parent : Fragment? = parentFragment
        if(parent is TicketVerificationV2){
            parent.closeFragment()
        }
    }

    companion object {
        fun newInstance(reservationId: Int) =
                TicketVerificationNoStubV2().apply {
                    arguments = Bundle().apply {
                        putInt(RESERVATION_ID, reservationId)
                    }
                }
    }
}
