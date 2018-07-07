package com.mobile.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants
import com.mobile.fragments.MPFragment
import com.mobile.utils.startCardIOActivity
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import kotlinx.android.synthetic.main.fragment_missing_billing.*
import javax.inject.Inject

class MissingBillingFragment : MPFragment(), MissingBillingFragmentView {

    @Inject
    lateinit var presenter: MissingBillingFragmentPresenter

    val creditCardTypeListener = object : CreditCardFormattingTextWatcher.CreditCardType {
        override fun setCardType(type: Int) {

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_missing_billing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        creditCardNumber
                .addTextChangedListener(CreditCardFormattingTextWatcher(creditCardNumber))
        expirationDate
                .addTextChangedListener(CreditCardExpiryTextWatcher(expirationDate))
        cancel.setOnClickListener {
            activity?.onBackPressed()
        }
        save.setOnClickListener {
            val data = BillingInfo(
                    creditCardNumber = creditCardNumber.text.toString(),
                    expiration = expirationDate.text.toString(),
                    securityCode = securityCode.text.toString(),
                    address = streetAddress.text.toString(),
                    address2 = streetAddress2.text.toString(),
                    city = city.text.toString(),
                    state = state.text.toString(),
                    zip = zip.text.toString()
            )
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

    override fun startCardIOActivity() {
        startCardIOActivity(Constants.CARD_SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if(requestCode==Constants.CARD_SCAN_REQUEST_CODE) {
            val scanResult = data?.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)?:return
            populateCard(scanResult)
        }
    }

    private fun populateCard(scanResult: CreditCard) {
        creditCardNumber.setText(scanResult.cardNumber)
        expirationDate.setText("${String.format("%02d",scanResult.expiryMonth)}/${scanResult.expiryYear.toString().substring(2)}")
        securityCode.setText(scanResult.cvv)
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = View.GONE
    }

    override fun showErrors(valid: BillingInfo) {
        creditCardNumber
                .error = valid.creditCardNumber
        expirationDate
                .error = valid.expiration
        securityCode
                .error = valid.securityCode
        streetAddress
                .error = valid.address
        city
                .error = valid.city
        state
                .error = valid.state
        zip
                .error = valid.zip
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