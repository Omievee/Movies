package com.mobile.profile

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_account_information.*
import javax.inject.Inject


class AccountInformation : MPFragment() {


    var updateData: Disposable? = null

    @Inject
    lateinit var userManager: com.mobile.session.UserManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setData()
        changeEmailTextView.setOnClickListener { showFragment(ChangeEmail()) }

    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun setData() {
        val cardNumber = SpannableStringBuilder(UserPreferences.userInfo.moviePassCardNumber.toString())
        val email = SpannableStringBuilder(UserPreferences.user.email.toString())
        val firstName = SpannableStringBuilder(UserPreferences.user.firstName.toString())
        val lastName = SpannableStringBuilder(UserPreferences.user.lastName.toString())

        moviepassCardNumber.text = cardNumber
        userEmail.text = email
        userName.text = firstName.append(" ").append(lastName)
    }


    fun retrieveLatestData() {
            updateData?.dispose()
            updateData = userManager
                    .getUserInfo()
                    .subscribe({
                        UserPreferences.userInfo = it
                        UserPreferences.user = it.user ?: return@subscribe
                        setData()
                    }, {
                        it.printStackTrace()

                    })
    }

    override fun onDestroy() {
        super.onDestroy()
        updateData?.dispose()
    }
}
