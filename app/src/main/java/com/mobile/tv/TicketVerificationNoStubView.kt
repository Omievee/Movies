package com.mobile.tv

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_ticket_verification_no_stub_view.view.*

class TicketVerificationNoStubView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {



    init {
        View.inflate(context, R.layout.layout_ticket_verification_no_stub_view, this)
    }

    fun bind(){
        noStubMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                charactersCounter.text = "${noStubMessage.text.toString().length}${'/'}${250}"
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    fun getReason(): String{
        return noStubMessage.text.toString()
    }

    fun onSubmit(clickListener: SubmitListener) {
        submitStub.setOnClickListener {
            if (!noStubMessage.text.isNullOrEmpty()) {
                clickListener.submitNoStubMessage()
            }else{
                Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface SubmitListener{
        fun submitNoStubMessage()
    }
}