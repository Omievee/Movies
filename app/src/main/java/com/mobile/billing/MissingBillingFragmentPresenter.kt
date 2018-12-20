package com.mobile.billing

import android.widget.EditText
import com.mobile.ApiError
import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.session.SessionManager
import com.moviepass.R
import io.reactivex.disposables.Disposable

class MissingBillingFragmentPresenter(
        val view: MissingBillingFragmentView,
        val sessionManager: SessionManager,
        val api: Api,
        val billingApi: BillingApi) {

    var saveSub: Disposable? = null
    var errorMessages: ErrorMessages? = null
    var userBillingInfo: Disposable? = null

    var creditCardChange = false
    var billingChange = false

    fun onCreate(errorMessages: ErrorMessages) {
        this.errorMessages = errorMessages
        getUserBillingInfo()
    }

    fun onDestroy() {
        saveSub?.dispose()
        userBillingInfo?.dispose()
    }

    fun onSaveClicked(data: BillingInfo) {
        view.clearFocus()
        view.hideKeyboard()
        view.hideErrors()
        val valid = isValid(data)
        if (valid != null) {
            return view.showErrors(valid)
        }
        if (!billingChange)
            data.billingAddress = null
        if (!creditCardChange)
            data.creditCardInfo = null
        saveSub?.dispose()
        view.showProgress()
        saveSub = billingApi
                .updateBilling(
                        data
                ).doAfterTerminate {
                    view.hideProgress()
                }.subscribe({
                    view.showSuccessDialog(R.string.billing_updated)
                }, { error ->
                    when (error) {
                        is ApiError -> view.showErrorDialog(error.error.message)
                        else -> view.showGenericError()
                    }
                })
    }

    fun creditCardTextEntered(editText: EditText) {
        if (editText.text.toString().isNotEmpty() && !CreditCardUtils.isNumeric(editText.text[0].toString())) {
            view.clearText(editText)
        }
    }

    private fun getUserBillingInfo() {
        userBillingInfo?.dispose()
        view.showProgress()

        userBillingInfo = billingApi.getSubscription().doAfterTerminate {
            view.hideProgress()
            view.setUpTextWatchers()
        }.subscribe({
            showBillingInfo(it.data.billingInfo)
        }, {

        })
    }

    private fun showBillingInfo(billingInfo: BillingInfo) {

        billingChange = true
        creditCardChange = true
        if (!billingInfo.billingAddress?.address1.isNullOrEmpty()) {
            view.showBillingAddress(billingInfo)
            billingChange = false
        }
        if (!billingInfo.creditCardInfo?.cardNumber.isNullOrEmpty()) {
            creditCardChange = false
            view.showBillingCreditCard(billingInfo)
        }
    }

    fun creditCardTextChanged() {
        view.showSaveAndCancel()
        creditCardChange = true
    }

    fun billingAddressChange() {
        view.showSaveAndCancel()
        billingChange = true
    }

    /**
     * null if valid
     */
    private fun isValid(data: BillingInfo): BillingInfo? {
        val validated = BillingInfo()

        if (creditCardChange) {
            validated.creditCardInfo = CreditCardInfo()
            data.creditCardInfo?.cardNumber = data.creditCardInfo?.cardNumber?.removeSpaces()
            validated.creditCardInfo?.cardNumber = when (data.creditCardInfo?.cardNumber.isNullOrEmpty()) {
                true -> errorMessages?.emptyCreditCardNumber
                false -> when (CreditCardUtils.isValid(data.creditCardInfo?.cardNumber)) {
                    false -> errorMessages?.invalidCreditCardNumber
                    true -> null
                }
            }
            val expy = when (data.creditCardInfo?.expirationYear != null && data?.creditCardInfo?.expirationMonth != null) {
                true -> "%s/%s".format(data.creditCardInfo?.expirationMonth, data.creditCardInfo?.expirationYear)
                else -> null
            }
            validated.creditCardInfo?.expirationYear = when (expy.isNullOrEmpty()) {
                true -> errorMessages?.emptyExpiration
                false -> when (CreditCardUtils.isValidDate(expy)) {
                    true -> null
                    false -> errorMessages?.invalidExpiration
                }
            }
            validated.creditCardInfo?.securityCode = when (data.creditCardInfo?.securityCode.isNullOrEmpty()) {
                true -> errorMessages?.needSecurityCode
                false -> when (CreditCardUtils.isValidSecurityCode(data.creditCardInfo?.securityCode)) {
                    true -> null
                    false -> errorMessages?.needSecurityCode
                }
            }
        }
        if (billingChange) {
            validated.billingAddress = BillingAddress()
            validated.billingAddress?.firstName = when(data.billingAddress?.firstName.isNullOrEmpty()) {
                true-> errorMessages?.needFirstName
                false->null
            }
            validated.billingAddress?.lastName = when(data.billingAddress?.lastName.isNullOrEmpty()) {
                true-> errorMessages?.needLastName
                false->null
            }
            validated.billingAddress?.address1 = when (data.billingAddress?.address1.isNullOrEmpty()) {
                true -> errorMessages?.needAddress
                false -> null
            }
            validated.billingAddress?.city = when (data.billingAddress?.city.isNullOrEmpty()) {
                true -> errorMessages?.needCity
                false -> null
            }
            validated.billingAddress?.state = when (data.billingAddress?.state?.length != 2) {
                true -> errorMessages?.needStateOrInvalid
                false -> null
            }
            validated.billingAddress?.postalCode = when (data.billingAddress?.postalCode?.isValidZip()) {
                true -> null
                else -> errorMessages?.needsValidZip
            }
        }

        return when (validated.allFieldsNull) {
            true -> null
            false -> validated
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

private fun getBillingInfo(data:SubscriptionData): BillingInfo {
    val billingAddress = data.billingInfo.billingAddress?.address1
    val billingAddress2 = data.billingInfo.billingAddress?.address2
    var city: String? = data.billingInfo.billingAddress?.city
    var state: String? = data.billingInfo.billingAddress?.state
    var zip: String? = data.billingInfo.billingAddress?.postalCode


    val creditNumber = data.billingInfo.creditCardInfo?.cardNumber
    var expiration: String? = when(data.billingInfo.creditCardInfo?.expirationYear) {
        null->"##/####"
        else-> "%s/%s".format(data.billingInfo.creditCardInfo?.expirationYear,data.billingInfo.creditCardInfo?.expirationMonth)
    }
    var cvv: String = when(data.billingInfo.creditCardInfo?.securityCode) {
        null-> "###"
        else-> data.billingInfo.creditCardInfo?.securityCode?:"###"
    }

    return BillingInfo(
            creditCardInfo = CreditCardInfo(
                    creditNumber,
                    cvv,
                    expiration
            ),
            billingAddress = BillingAddress(
                    address1 = billingAddress,
                    address2 = billingAddress2,
                    city = city,
                    state = state,
                    postalCode = zip
            ))
}

val BillingInfo.allFieldsNull: Boolean
    get() {
        val fields = arrayOf(
                creditCardInfo?.cardNumber,
                creditCardInfo?.expirationYear,
                creditCardInfo?.expirationMonth,
                creditCardInfo?.securityCode,
                billingAddress?.firstName,
                billingAddress?.lastName,
                billingAddress?.address1,
                billingAddress?.city,
                billingAddress?.state,
                billingAddress?.postalCode
        )
        return fields.count { it == null } == fields.size
    }

data class ErrorMessages(
        val needFirstName:String,
        val needLastName:String,
        val invalidCreditCardNumber: String,
        val emptyCreditCardNumber: String,
        val invalidExpiration: String,
        val emptyExpiration: String,
        val needSecurityCode: String,
        val needAddress: String,
        val needCity: String,
        val needStateOrInvalid: String,
        val needsValidZip: String
)