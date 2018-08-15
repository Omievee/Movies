package com.mobile.tv

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.mobile.widgets.MPAlertDialog
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_ticket_verification_no_stub_view.view.*

class TicketVerificationNoStubView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {


    var listener: SubmitListener? = null

    init {
        View.inflate(context, R.layout.layout_ticket_verification_no_stub_view, this)
        onSubmit()
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

    fun bind(listener: SubmitListener) {
        this.listener = listener
    }

    fun getReason(): String {
        return noStubMessage.text.toString()
    }

    fun onSubmit() {
        submitStub.setOnClickListener {
            if (!noStubMessage.text.isNullOrEmpty()) {
                listener?.submitNoStubMessage()
            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun displayWarning() {
        val context = context ?: return
        MPAlertDialog(context)
                .setTitle(R.string.activity_verification_lost_ticket_title_post)
                .setCancelable(false)
                .setOnDismissListener { listener?.closeFragment() }
                .setMessage(R.string.activity_verification_lost_ticket_message_post)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    listener?.closeFragment()
                }
                .show()
    }

    interface SubmitListener {
        fun submitNoStubMessage()
        fun closeFragment()
    }
}