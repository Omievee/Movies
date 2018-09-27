package com.mobile.profile

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.mobile.requests.ChangeEmailRequest
import com.mobile.responses.ChangeEmailResponse
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_change_email.*
import javax.inject.Inject


class ChangeEmail : MPFragment(), View.OnClickListener {

    @Inject
    lateinit var userManager: com.mobile.session.UserManager


    internal lateinit var newEmail: EditText
    internal lateinit var currentPassword: EditText
    var changeEmailDisposable: Disposable? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newEmail = newEmailEditText
        currentPassword = currentPasswordEditText

        saveChanges.setOnClickListener(this)
        cancelChanges.setOnClickListener(this)

        newEmail.addTextChangedListener(CustomTextWatcher())
        currentPassword.addTextChangedListener(CustomTextWatcher())

    }


    fun closeKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun enableSave() {
        saveChanges.visibility = View.VISIBLE
        cancelChanges.visibility = View.VISIBLE
    }

    fun disableSave() {
        saveChanges.visibility = View.INVISIBLE
        cancelChanges.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveChanges -> if (valid()) {
                updateEmail()
            }

            R.id.cancelChanges -> {
                disableSave()
                closeKeyboard()
                newEmailTextInputLayout.error = ""
                currentPasswordTextInputLayout.error = ""
                newEmailEditText.text.clear()
                currentPasswordEditText.text.clear()
                newEmailEditText.clearFocus()
                currentPasswordEditText.clearFocus()
            }
        }
    }


    fun valid(): Boolean {
        newEmailTextInputLayout.error = ""
        currentPasswordTextInputLayout.error = ""
        var valid = true
        if (newEmailEditText.getText().toString().trim { it <= ' ' }.isEmpty()) {
            valid = false
            newEmailTextInputLayout.error = "Enter a valid email address"
        }
        if (currentPasswordEditText.text.toString().trim { it <= ' ' }.isEmpty() || currentPasswordEditText.getText().toString().trim { it <= ' ' }.length < 6 || currentPasswordEditText.getText().toString().trim { it <= ' ' }.length > 20) {
            valid = false
            currentPasswordTextInputLayout.error = "Enter a valid password"
        }
        return valid
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        changeEmailDisposable?.dispose()
    }

    private fun updateEmail() {
        val userId = UserPreferences.user.id
        val updatedEmail = newEmailEditText.text.toString().replace(" ", "")
        val pw = currentPasswordEditText.text.toString().replace(" ", "")
        val request = ChangeEmailRequest(updatedEmail, pw, userId)

        changeEmailDisposable?.dispose()
        changeEmailDisposable = userManager
                .updateUserEmail(request)
                .subscribe({
                    successfulUpdate(it)
                }, { error ->
                    when (error) {
                        is ApiError -> Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(context, R.string.generic_error, Toast.LENGTH_LONG).show()
                    }

                })

    }

    private fun successfulUpdate(it: ChangeEmailResponse?) {
        Toast.makeText(context, it?.message, Toast.LENGTH_LONG).show()
        when (parentFragment) {
            is AccountInformation -> {
                (parentFragment as AccountInformation).retrieveLatestData()
            }
        }

        activity?.onBackPressed()
    }

    inner class CustomTextWatcher : TextWatcher {


        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (newEmail.text.toString().trim { it <= ' ' }.isEmpty() && currentPassword.text.toString().trim { it <= ' ' }.isEmpty()) {
                disableSave()
            } else {
                enableSave()
            }
        }
    }

}
