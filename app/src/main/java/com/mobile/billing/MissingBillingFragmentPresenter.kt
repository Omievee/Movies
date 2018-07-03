package com.mobile.billing

import com.mobile.network.Api
import com.mobile.session.SessionManager
import io.reactivex.disposables.Disposable

class MissingBillingFragmentPresenter(
        val view: MissingBillingFragmentView,
        val sessionManager: SessionManager,
        val api: Api) {

    var saveSub: Disposable? = null
    var errorMessages: ErrorMessages? = null

    fun onCreate(errorMessages: ErrorMessages) {
        this.errorMessages = errorMessages
    }

    fun onDestroy() {

    }

    fun onSaveClicked(data: BillingInfo) {
        val valid = isValid(data)
        if (valid != null) {
            return view.showErrors(valid)
        }
        saveSub?.dispose()
        view.showProgress()
        saveSub = api
                .updateBilling(sessionManager.getUser()?.id ?: 0,
                        data
                ).subscribe({

                }, { error ->
                })
    }

    /**
     * null if valid
     */
    private fun isValid(data: BillingInfo): BillingInfo? {
        val validated = BillingInfo()
        data.creditCardNumber = data.creditCardNumber?.removeSpaces()
        validated.creditCardNumber = when (data.creditCardNumber.isNullOrEmpty()) {
            true -> errorMessages?.emptyCreditCardNumber
            false -> when (CreditCardUtils.isValid(data.creditCardNumber)) {
                false -> errorMessages?.invalidCreditCardNumber
                true -> null
            }
        }
        validated.expiration = when (data.expiration.isNullOrEmpty()) {
            true -> errorMessages?.emptyExpiration
            false -> when (CreditCardUtils.isValidDate(data.expiration)) {
                true -> null
                false -> errorMessages?.invalidExpiration
            }
        }
        validated.securityCode = when (data.securityCode?.length ?: 0 < 3) {
            false -> null
            true -> errorMessages?.needSecurityCode
        }
        validated.address = when (data.address.isNullOrEmpty()) {
            true -> errorMessages?.needAddress
            false -> null
        }
        validated.city = when (data.city.isNullOrEmpty()) {
            true -> errorMessages?.needCity
            false -> null
        }
        validated.state = when (data.state?.length != 2) {
            true -> errorMessages?.needStateOrInvalid
            false -> null
        }
        validated.zip = when(data.zip?.isValidZip()) {
            true-> null
            else-> errorMessages?.needsValidZip
        }
        return when(validated.allFieldsNull) {
            true->null
            false->validated
        }
    }

    fun onCameraClicked() {
        view.startCardIOActivity()
    }
}

fun String.removeSpaces(): String {
    return filter {
        Character.isDigit(it)
    }
}

fun String.isValidZip(): Boolean {
    val size = count {
        Character.isDigit(it)
    }
    return size == 5 || size == 9
}

val BillingInfo.allFieldsNull:Boolean
get() {
    val fields = arrayOf(
            creditCardNumber,
            expiration,
            securityCode,
            address,
            city,
            state,
            zip
    )
    return fields.count { it==null } == fields.size
}

data class ErrorMessages(
        val invalidCreditCardNumber: String,
        val emptyCreditCardNumber: String,
        val invalidExpiration: String,
        val emptyExpiration: String,
        val needSecurityCode: String,
        val needAddress: String,
        val needCity:String,
        val needStateOrInvalid:String,
        val needsValidZip:String
)