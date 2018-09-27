package com.mobile.profile

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.mobile.requests.ChangePasswordRequest
import com.mobile.session.UserManager
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_change_password.*
import javax.inject.Inject

class ChangePassword : MPFragment(), View.OnClickListener {


    private var firstTime = true


    @Inject
    lateinit var userManager: UserManager

    var changePasswordDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textChangeListeners()
        saveChanges.setOnClickListener(this)
        cancelPWChange.setOnClickListener(this)
    }

    private fun textChangeListeners() {
        oldPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (firstTime) {
                    enableSaveAndCancel()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        password1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (firstTime) {
                    enableSaveAndCancel()
                    password2.isEnabled = true
                    firstTime = false
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun enableSaveAndCancel() {
        context?.let {
            saveChanges.isClickable = true
            cancelPWChange.isClickable = true
            cancelPWChange.setTextColor(ContextCompat.getColor(it, R.color.almost_white))
            saveChanges.setTextColor(ContextCompat.getColor(it, R.color.new_red))
        }
    }

    private fun disableSaveAndCancel() {
        context?.let {
            firstTime = true
            saveChanges.isClickable = false
            cancelPWChange.isClickable = false
            cancelPWChange.setTextColor(ContextCompat.getColor(it, R.color.gray_icon))
            saveChanges.setTextColor(ContextCompat.getColor(it, R.color.gray_icon))
            password1.setText("")
            password2.setText("")
            oldPassword.setText("")
            password2.isEnabled = false
            password1.clearFocus()
            password2.clearFocus()
            oldPassword.clearFocus()
            password2TextInputLayout.error = null
            password1TextInputLayout.error = null
            oldPasswordTextInputLayout.error = null
            firstTime = true
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }

    }

    override fun onClick(it: View?) {
        when (it?.id) {
            R.id.saveChanges -> {
                password1TextInputLayout.error = null
                password2TextInputLayout.error = null
                oldPasswordTextInputLayout.error = null
                password1.clearFocus()
                password2.clearFocus()
                oldPassword.clearFocus()
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)

                if (password1.text.toString().trim { it <= ' ' }.equals(password2.text.toString().trim { it <= ' ' }, ignoreCase = true) && !oldPassword.text.toString().trim { it <= ' ' }.isEmpty()) {
                    if (password1.text.toString().length >= 6) {
                        if (!password1.text.toString().trim { it <= ' ' }.equals(oldPassword.text.toString().trim { it <= ' ' }, ignoreCase = true)) {
                            progress.visibility = View.VISIBLE
                            changePassword();
                        } else {
                            oldPasswordTextInputLayout.error = resources.getString(R.string.fragment_profile_account_information_new_password_same_as_old)
                        }
                    } else {
                        if (oldPassword.text.toString().trim { it <= ' ' }.isEmpty())
                            oldPasswordTextInputLayout.error = resources.getString(R.string.fragment_profile_account_information_old_password_empty)
                        if (password1.text.toString().trim { it <= ' ' }.isEmpty())
                            password1TextInputLayout.error = resources.getString(R.string.fragment_profile_account_information_password_empty)
                        else
                            password1TextInputLayout.error = resources.getString(R.string.fragment_profile_account_information_password_more_than_6_characters)
                    }
                } else {
                    if (oldPassword.text.toString().trim { it <= ' ' }.isEmpty())
                        oldPasswordTextInputLayout.error = resources.getString(R.string.fragment_profile_account_information_old_password_empty)
                    password2TextInputLayout.error = resources.getString(R.string.fragment_profile_account_information_password_match)
                }
            }
            R.id.cancelPWChange -> activity?.onBackPressed()
        }
    }

    private fun changePassword() {
        val oldPw = oldPassword.text.toString().trim()
        val newPw = password1.text.toString().trim()
        val uId = UserPreferences.user.id
        val request = ChangePasswordRequest(oldPw, newPw, uId)

        changePasswordDisposable?.dispose()
        changePasswordDisposable = userManager
                .updateUserPassword(request)
                .subscribe({
                    Toast.makeText(context, getString(R.string.profile_password_change), Toast.LENGTH_LONG).show()
                    disableSaveAndCancel()
                    activity?.onBackPressed()
                }, { error ->
                    when (error) {
                        is ApiError -> {
                            val er = error.error.error ?: return@subscribe
                            if (er.contains("Current password does not match for user")) {
                                Toast.makeText(context, getString(R.string.profile_password_mismatch), Toast.LENGTH_LONG).show()
                            }
                            progress.visibility = View.GONE
                        }
                        else -> Toast.makeText(context, R.string.generic_error, Toast.LENGTH_LONG).show()
                    }
                })

    }


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        changePasswordDisposable?.dispose()
    }
}
