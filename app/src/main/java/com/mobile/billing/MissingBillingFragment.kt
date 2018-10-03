package com.mobile.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import com.mobile.Constants
import com.mobile.fragments.MPFragment
import com.mobile.keyboard.KeyboardManager
import com.mobile.textwatcher.MPTextWatcher
import com.mobile.textwatcher.TextWatcherInterface
import com.mobile.utils.startCardIOActivity
import com.mobile.widgets.MPAlertDialog
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import kotlinx.android.synthetic.main.fragment_missing_billing.*
import javax.inject.Inject



class MissingBillingFragment : MPFragment(), MissingBillingFragmentView {

    @Inject
    lateinit var presenter: MissingBillingFragmentPresenter

    @Inject
    lateinit var keyboardManager: KeyboardManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_missing_billing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        setBillingDescriptions()

        creditCardLayout
                .addTextChangedListener(CreditCardFormattingTextWatcher(creditCardLayout.editText))
        expirationDateLayout
                .addTextChangedListener(CreditCardExpiryTextWatcher(expirationDateLayout.editText))


        cancel.setOnClickListener {
            activity?.onBackPressed()
        }

        save.setOnClickListener {
            var paymentInfo = PaymentInfo(
                    creditCardLayout.text,
                    securityCodeLayout.text,
                    expirationDateLayout.text)
            var billingAddress = BillingAddress(
                    streetAddressLayout.text,
                    streetAddressLayout2.text,
                    cityLayout.text,
                    stateLayout.text,
                    zipLayout.text
            )
            val data = BillingInfo(getString(R.string.section_credit_card_data), paymentInfo, billingAddress)
            presenter.onSaveClicked(data)
        }

        presenter.onCreate(
                ErrorMessages(
                        invalidCreditCardNumber = getString(R.string.invalid_credit_card_number),
                        invalidExpiration = getString(R.string.invalid_exp),
                        emptyCreditCardNumber = getString(R.string.invalid_credit_card_number),
                        emptyExpiration = getString(R.string.invalid_exp),
                        needSecurityCode = getString(R.string.invalid_cvv),
                        needStateOrInvalid = getString(R.string.address_invalid_state),
                        needAddress = getString(R.string.address_invalid_address),
                        needCity = getString(R.string.address_invalid_city),
                        needsValidZip = getString(R.string.address_invalid_zip)
                )
        )
        cameraFL.setOnClickListener {
            presenter.onCameraClicked()
        }
    }

    private fun setBillingDescriptions(){
        val builderOne = SpannableStringBuilder(billingDescriptionOne.text.toString()).apply {
            setSpan(ForegroundColorSpan(Color.RED), billingDescriptionOne.text.toString().length -1, billingDescriptionOne.text.toString().length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        billingDescriptionOne.text = builderOne

        val builderTwo = SpannableStringBuilder(billingDescriptionTwo.text.toString()).apply {
            setSpan(ForegroundColorSpan(Color.RED), 0, 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        billingDescriptionTwo.text = builderTwo
    }

    override fun hideKeyboard() {
        keyboardManager.hide()
    }

    override fun startCardIOActivity() {
        startCardIOActivity(Constants.CARD_SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            val scanResult = data?.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)
                    ?: return
            populateCard(scanResult)
        }
    }

    private fun populateCard(scanResult: CreditCard) {
        hideKeyboard()
        creditCardLayout.text = scanResult.cardNumber
        expirationDateLayout.text = "${String.format("%02d", scanResult.expiryMonth)}/${scanResult.expiryYear.toString().substring(2)}"
        securityCodeLayout.text = scanResult.cvv
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = View.GONE
    }

    override fun showErrors(valid: BillingInfo) {
        creditCardLayout
                .error = valid.paymentInfo?.number
        expirationDateLayout
                .error = valid.paymentInfo?.expirationDate
        securityCodeLayout
                .error = valid.paymentInfo?.cvv
        streetAddressLayout
                .error = valid.billingAddress?.street
        cityLayout
                .error = valid.billingAddress?.city
        stateLayout
                .error = valid.billingAddress?.state
        zipLayout
                .error = valid.billingAddress?.zip
    }

    override fun hideErrors() {
        arrayOf(creditCardLayout,
                expirationDateLayout,
                securityCodeLayout,
                streetAddressLayout,
                cityLayout,
                stateLayout,
                zipLayout)
                .forEach {
                    it.error = null
                }
    }


    override fun showBillingAddress(billingInfo: BillingInfo) {
        streetAddressLayout.text = billingInfo.billingAddress?.street
        cityLayout.text = billingInfo.billingAddress?.city
        stateLayout.text = billingInfo.billingAddress?.state
        zipLayout.text = billingInfo.billingAddress?.zip
    }

    override fun showBillingCreditCard(billingInfo: BillingInfo) {
        creditCardLayout.text = billingInfo.paymentInfo?.number
        securityCodeLayout.text = billingInfo.paymentInfo?.cvv
        expirationDateLayout.text = billingInfo.paymentInfo?.expirationDate
    }

    override fun showSaveAndCancel() {
        save.visibility = View.VISIBLE
        cancel.visibility = View.VISIBLE
    }

    override fun hideSaveAndCancel() {
        save.visibility = View.INVISIBLE
        cancel.visibility = View.INVISIBLE
    }

    override fun showErrorDialog(message: String) {
        val context = context ?: return
        MPAlertDialog(context)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->

                }
                .show()
    }

    override fun showGenericError() {
        val context = context ?: return
        MPAlertDialog(context)
                .setMessage(getString(R.string.generic_error))
                .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                .show()
    }

    override fun showSuccessDialog(message: Int) {
        val context = context ?: return
        MPAlertDialog(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    activity?.onBackPressed()
                }
                .show()
    }

    override fun clearFocus() {
        arrayOf(creditCardLayout.editText,
                expirationDateLayout.editText,
                securityCodeLayout.editText,
                streetAddressLayout.editText,
                streetAddressLayout2.editText,
                cityLayout.editText,
                stateLayout.editText,
                zipLayout.editText)
                .forEach {
                    it.clearFocus()
                }
    }

    override fun clearText(editText: EditText) {
        editText.text.clear()
    }

    override fun setUpTextWatchers() {

        var watcher = MPTextWatcher()

        watcher.registerEditText(creditCardLayout.editText)
                .registerEditText(expirationDateLayout.editText)
                .registerEditText(securityCodeLayout.editText)
                .setCallBack(object : TextWatcherInterface {
                    override fun afterTextChanged(s: Editable?, editText: EditText) {
                        watcher.unregisterEditText(editText)
                        presenter.creditCardTextChanged()
                        presenter.creditCardTextEntered(editText)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, editText: EditText) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }
                })

        MPTextWatcher().registerEditText(streetAddressLayout.editText)
                .registerEditText(streetAddressLayout2.editText)
                .registerEditText(cityLayout.editText)
                .registerEditText(stateLayout.editText)
                .registerEditText(zipLayout.editText)
                .setCallBack(object : TextWatcherInterface {
                    override fun afterTextChanged(s: Editable?, editText: EditText) {
                        presenter.billingAddresChange()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, editText: EditText) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }
                })
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}