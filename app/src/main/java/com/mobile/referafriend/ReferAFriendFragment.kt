package com.mobile.referafriend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.mobile.helpers.MPAlertDialog
import com.mobile.utils.onBackExtension
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_refer_a_friend_v2.*
import javax.inject.Inject


class ReferAFriendFragment : MPFragment(), ReferAFriendView {

    @Inject
    lateinit var presenter: ReferAFriendFragmentPresenter


    override fun showProgress() {
        activity?: return
        progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        activity?: return
        progress.visibility = View.GONE
    }

    override fun setReferralsInfo(title: String, message: String) {
        referralsMessage.text = message
        referralsTitle.text = title
    }

    override fun showErrorDialog(message: String) {
        val context = context ?: return
        MPAlertDialog(context)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok)){_,_ ->}
                .show()
    }

    override fun showErrorDialog(message: Int) {
        val context = context ?: return
        MPAlertDialog(context)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok)){_,_ ->}
                .show()
    }

    override fun startEmailActivity(intent: Intent) {
        startActivity(intent)
    }

    override fun showErrors(errors: Referral) {
        emailInputLayout.error = errors.email
        firstNameInputLayout.error = errors.firstName
        lastNameInputLayout.error = errors.lastName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_refer_a_friend_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onCreate(
                ErrorMessages(
                        invalidEmail = getString(R.string.referral_email_error_message),
                        invalidFirstName = getString(R.string.required_field),
                        invalidLastName = getString(R.string.required_field)
                )
        )

        submitReferral.setOnClickListener{
            presenter.onSubmitClicked(
                    Referral(
                            email.text.toString(),
                            fistName.text.toString(),
                            lastName.text.toString()
                    )
            )
        }

        backButton.setOnClickListener{
            parentFragment?.onBackExtension()
        }
    }

    override fun showGenericError() {
        val context = context ?: return
        MPAlertDialog(context)
                .setMessage(getString(R.string.generic_error))
                .setPositiveButton(getString(R.string.ok)){_,_ ->}
                .show()
    }

    override fun removeErrors() {
        arrayOf(emailInputLayout,firstNameInputLayout,lastNameInputLayout).forEach {
            it.error = ""
        }
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }
}