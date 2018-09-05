package com.mobile.referafriend

import android.content.Intent
import android.net.Uri
import android.text.Html
import com.mobile.ApiError
import com.mobile.network.Api
import com.mobile.responses.ReferAFriendResponse
import io.reactivex.disposables.Disposable

class ReferAFriendFragmentPresenter(val api: Api, val view: ReferAFriendView){

    var referalDisposable: Disposable? = null
    var referalResponse: ReferAFriendResponse? = null
    var errorMessages: ErrorMessages? = null

    fun onCreate(errorMessages: ErrorMessages){
        this.errorMessages = errorMessages
        getReferralInfo()
    }

    private fun islValid(referral: Referral): Referral? {

        val validation = Referral()
        validation.email = when (isEmailValid(referral.email)){
            true -> null
            false -> errorMessages?.invalidEmail
        }

        validation.firstName = when(referral.firstName.isNullOrEmpty()){
            false -> null
            true -> errorMessages?.invalidFirstName
        }

        validation.lastName = when(referral.lastName.isNullOrEmpty()){
            false -> null
            true -> errorMessages?.invalidLastName
        }

        return when(validation.lastName.isNullOrEmpty() && validation.email.isNullOrEmpty() && validation.firstName.isNullOrEmpty()){
            true -> null
            false -> validation
        }

    }

    private fun isEmailValid(email: String?): Boolean{
        return (!email.isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    private fun getReferralInfo() {

        referalDisposable?.dispose()
        view.showProgress()

        referalDisposable = api.referAFriend().doAfterTerminate {
            view.hideProgress()
        }.subscribe({
            when (it) {
                null -> {
                    view.showGenericError()
                }
                else -> {
                    referalResponse = it
                    view.setReferralsInfo(it.referralTitle, it.referralMessage)
                }
            }
        }, { error ->
            when(error){
                is ApiError -> view.showErrorDialog(error.error.message)
                else -> view.showGenericError()

            }
        })
    }

    fun onSubmitClicked(referral: Referral) {
        val valid = islValid(referral)
        if(valid != null){
            return view.showErrors(valid)
        }
        view.removeErrors()
        when(referalResponse){
            null -> view.showGenericError()
            else -> referFriend(referral)
        }
    }

    private fun referFriend(referal: Referral){
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "application/octet-stream"
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "message/rfc822"
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, referalResponse?.getEmailSubject())
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(referal.email ?: ""))
        val emailMessege = Html.fromHtml(referalResponse?.getEmailMessage())
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey " + referal.firstName + " " + referal.lastName + "," + emailMessege)
        view.startEmailActivity(emailIntent)
    }

    fun onDestroy(){
        referalDisposable?.dispose()
    }

}

data class ErrorMessages(
        val invalidFirstName: String,
        val invalidLastName: String,
        val invalidEmail: String
)