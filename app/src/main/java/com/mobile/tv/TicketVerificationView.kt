package com.mobile.tv

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.mobile.model.PopInfo
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_theaterpolicy.view.*
import kotlinx.android.synthetic.main.layout_ticket_verification_no_reservation_available.view.*

class TicketVerificationView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs){

    init {
        View.inflate(context, R.layout.layout_ticket_verification_no_reservation_available,this)
    }

    fun bind(popInfo: PopInfo, isTicketRedeemed: Boolean){
        ticketVerificationMovieTitle.text = popInfo.movieTitle

        ticketVerificationMessage.text = when(isTicketRedeemed){
            false -> resources.getString(R.string.ticket_verification_no_redeemed)
            true -> resources.getString(R.string.ticket_verification_redeemed)
        }

        closeButton.visibility = when(isTicketRedeemed){
            true -> View.GONE
            false -> View.VISIBLE
        }

    }

    fun setOnClickListeners(clickListeners: ClickListeners?){
        ticketVerificationNoTicketStub.setOnClickListener {
            clickListeners?.noTicketSub()
        }

        ticketVerificationHelp.setOnClickListener {
            clickListeners?.getHelp()
        }

        ticketVerificationCameraIcon.setOnClickListener {
            clickListeners?.takePicture()
        }

        closeButton.setOnClickListener {
            clickListeners?.close()
        }
    }

    fun showProgress(){
        progress.visibility = View.VISIBLE
    }

    fun hideProgress(){
        progress.visibility = View.GONE
    }

    interface ClickListeners{
        fun noTicketSub()
        fun getHelp()
        fun takePicture()
        fun close()
    }





}