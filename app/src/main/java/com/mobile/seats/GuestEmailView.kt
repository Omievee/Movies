package com.mobile.seats

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.widget.EditText
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_guest_email.view.*

class GuestEmailView(context: Context?, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    var email: GuestEmail? = null
        get() {
            val emailF = field
            val email = this.editText.text?.toString()?.trim()
            emailF?.email = when (email.isNullOrEmpty()) {
                true -> null
                else -> email
            }
            return emailF
        }

    var error: String? = null
        set(value) {
            field = value
            inputLayout.error = value
        }

    init {
        inflate(context, R.layout.layout_guest_email, this)
    }

    val emailText: EditText
        get() {
            return editText
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        emailText.isEnabled = enabled
    }

    fun bind(guestEmail: GuestEmail) {
        this.email = guestEmail
        inputLayout.hint =
                resources.getString(R.string.guest_email_hint, (guestEmail.index ?: 0) + 1)
        editText.setText(guestEmail.email)
        inputLayout.error = when (guestEmail.status) {
            null, EmailStatus.APPROVED -> {
                null
            }
            else -> {
                resources.getString(R.string.already_moviepass_member)
            }
        }
    }

}