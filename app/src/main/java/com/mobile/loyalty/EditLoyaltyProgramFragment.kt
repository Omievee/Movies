package com.mobile.loyalty


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.mobile.fragments.MPFragment
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_edit_loyalty_program.*
import javax.inject.Inject


private const val SELECTED_THEATER_CHAIN = "theaterChain"


class EditLoyaltyProgramFragment : MPFragment(), EditLoyalProgramView {


    private var loyaltyProgram: TheaterChain? = null
    lateinit var data: List<Triple<String, RequiredField, String>>
    lateinit var theaterChain: TheaterChain
    lateinit var oldCardNumber: String

    @Inject
    lateinit var presenter: EditLoyaltyProgramPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loyaltyProgram = arguments?.getParcelable(SELECTED_THEATER_CHAIN) ?: return@onCreate
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_loyalty_program, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onViewCreated()
        editLoyaltyNumber.setOnClickListener { showUpdateLoyaltyFields() }


    }


    override fun setLoyaltyData() {
        selectedloyaltyProgram.text = loyaltyProgram?.chainName
        val cardNumber = SpannableStringBuilder(loyaltyProgram?.getRequiredFields().toString())
        loyaltyCardNumber.text = cardNumber
        oldCardNumber = cardNumber.toString()
    }

    override fun showUpdateLoyaltyFields() {
        editLoyaltyNumber.visibility = View.GONE
        deleteInput.visibility = View.VISIBLE
        updateView.visibility = View.VISIBLE

        loyaltyCardNumber.isEnabled = true
        deleteInput.setOnClickListener { loyaltyCardNumber.text.clear() }


        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(loyaltyCardNumber, InputMethodManager.SHOW_FORCED)


        loyaltyProgram?.let { theaterChain ->
            this.theaterChain = theaterChain
            updateLoyaltyButton.setOnClickListener { v ->
                mapData(theaterChain)
                presenter.userUpdatedLoyaltyCard(theaterChain, data = data)
            }
            deleteLoyalty.setOnClickListener {
                mapData(theaterChain)
                showAlert(null, resources.getString(R.string.loyalty_delete_program), true)

            }
        }
    }

    fun mapData(theaterChain: TheaterChain) {
        val fieldNameToValue = mutableMapOf<String, Pair<RequiredField, String>>()

        val newNumber = loyaltyCardNumber.text
        val type = theaterChain.requiredFields ?: return
        val cardNumber = "cardNumber"

        fieldNameToValue[cardNumber] = Pair(type, newNumber.toString())
        data = fieldNameToValue.map {
            Triple(it.key, it.value.first, it.value.second)
        }
    }


    override fun updateFailure(failure: String) {
        showAlert(failure, null, false)
    }

    private fun showAlert(failureString: String? = null, loyaltyDelete: String? = null, delete: Boolean) {
        val displayString: String? = if (failureString != null) {
            failureString
        } else {
            loyaltyDelete
        }

        AlertDialog
                .Builder(context, R.style.CUSTOM_ALERT)
                .setMessage(displayString)
                .setPositiveButton("Ok") { _, _ ->
                    if (delete) {
                        presenter.deleteLoyaltyProgram(theaterChain, data)
                    }
                }
                .setCancelable(true)
                .show()

    }


    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun hideProgress() {
        loyaltyProgress.visibility = View.GONE
    }

    override fun showProgress() {
        loyaltyProgress.visibility = View.VISIBLE
    }

    override fun updateSuccessful(delete: Boolean) {
        activity?.let {
            if (delete) {
                Toast.makeText(it, getString(R.string.edit_loyalty_number_delete), Toast.LENGTH_LONG).show()
                when (parentFragment) {
                    is LoyaltyProgramFragment -> (parentFragment as LoyaltyProgramFragment).onLoyaltyDataRemoved()
                }
                it.onBackPressed()
            } else {
                Toast.makeText(it, getString(R.string.edit_loyalty_number_update), Toast.LENGTH_LONG).show()
                it.onBackPressed()
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(theater: TheaterChain) =
                EditLoyaltyProgramFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(SELECTED_THEATER_CHAIN, theater)
                    }
                }
    }
}
