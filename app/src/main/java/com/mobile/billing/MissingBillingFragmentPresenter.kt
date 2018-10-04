package com.mobile.billing

import android.widget.EditText
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.network.Api
import com.mobile.responses.UserInfoResponse
import com.mobile.session.SessionManager
import com.moviepass.R
import io.reactivex.disposables.Disposable

class MissingBillingFragmentPresenter(
        val view: MissingBillingFragmentView,
        val sessionManager: SessionManager,
        val api: Api) {

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
            data.paymentInfo = null
        saveSub?.dispose()
        view.showProgress()
        saveSub = api
                .updateBilling(sessionManager.getUser()?.id ?: 0,
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

        userBillingInfo = api.getUserDataRx(UserPreferences.userId).doAfterTerminate {
            view.hideProgress()
            view.setUpTextWatchers()
        }.subscribe({
            showBillingInfo(getBillingInfo(it))
        }, {

        })
    }

    private fun showBillingInfo(billingInfo: BillingInfo) {

        billingChange = true
        creditCardChange = true
        if (!billingInfo.billingAddress?.street.isNullOrEmpty()) {
            view.showBillingAddress(billingInfo)
            billingChange = false
        }
        if (!billingInfo.paymentInfo?.number.isNullOrEmpty()) {
            creditCardChange = false
            view.showBillingCreditCard(billingInfo)
        }
    }

    fun creditCardTextChanged() {
        view.showSaveAndCancel()
        creditCardChange = true
    }

    fun billingAddresChange() {
        view.showSaveAndCancel()
        billingChange = true
    }

    /**
     * null if valid
     */
    private fun isValid(data: BillingInfo): BillingInfo? {
        val validated = BillingInfo()
        if (creditCardChange) {
            validated.paymentInfo = PaymentInfo()
            data.paymentInfo?.number = data.paymentInfo?.number?.removeSpaces()
            validated.paymentInfo?.number = when (data.paymentInfo?.number.isNullOrEmpty()) {
                true -> errorMessages?.emptyCreditCardNumber
                false -> when (CreditCardUtils.isValid(data.paymentInfo?.number)) {
                    false -> errorMessages?.invalidCreditCardNumber
                    true -> null
                }
            }
            validated.paymentInfo?.expirationDate = when (data.paymentInfo?.expirationDate.isNullOrEmpty()) {
                true -> errorMessages?.emptyExpiration
                false -> when (CreditCardUtils.isValidDate(data.paymentInfo?.expirationDate)) {
                    true -> null
                    false -> errorMessages?.invalidExpiration
                }
            }
            validated.paymentInfo?.cvv = when (data.paymentInfo?.cvv.isNullOrEmpty()) {
                true -> errorMessages?.needSecurityCode
                false -> when (CreditCardUtils.isValidSecurityCode(data.paymentInfo?.cvv)) {
                    true -> null
                    false -> errorMessages?.needSecurityCode
                }
            }
        }
        if (billingChange) {
            validated.billingAddress = BillingAddress()
            validated.billingAddress?.street = when (data.billingAddress?.street.isNullOrEmpty()) {
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
            validated.billingAddress?.zip = when (data.billingAddress?.zip?.isValidZip()) {
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

val BillingInfo.allFieldsNull: Boolean
    get() {
        val fields = arrayOf(
                paymentInfo?.number,
                paymentInfo?.expirationDate,
                paymentInfo?.cvv,
                billingAddress?.street,
                billingAddress?.city,
                billingAddress?.state,
                billingAddress?.zip
        )
        return fields.count { it == null } == fields.size
        return false
    }

private fun getBillingInfo(user: UserInfoResponse): BillingInfo {
    val billingAddress = user.billingAddressLine2
    val billingAddressList = billingAddress?.split(",".toRegex(), 0)
    var city: String? = null
    var state: String? = null
    var zip: String? = null

    if (billingAddressList?.size ?: 0 >= 3) {
        city = billingAddressList?.get(0)?.trim()
        state = billingAddressList?.get(1)?.trim()
        zip = billingAddressList?.get(2)?.trim()
    }

    val creditNumber = user.billingCard
    var expiration: String? = null
    var CVV: String? = null
    if (!creditNumber.isNullOrEmpty()) {
        expiration = "##/####"
        CVV = "###"
    }

    return BillingInfo(null,
            PaymentInfo(
                    creditNumber,
                    CVV,
                    expiration
            ),
            BillingAddress(
                    user.billingAddressLine1,
                    null,
                    city,
                    state,
                    zip
            ))
}


data class ErrorMessages(
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