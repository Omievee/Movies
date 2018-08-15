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
        loyaltyProgram?.let {
            presenter.onViewCreated(it)
        }
        editLoyaltyNumber.setOnClickListener { showUpdateLoyaltyFields() }
    }


    override fun setLoyaltyData() {
        selectedloyaltyProgram.text = loyaltyProgram?.chainName
        val cardNumber = SpannableStringBuilder(loyaltyProgram?.getRequiredFields().toString())
        loyaltyCardNumber.text = cardNumber
    }

    override fun updateLoyaltyProgramInfo() {

    }

    override fun hideProgress() {
        loyaltyProgress.visibility = View.GONE
    }

    override fun showProgress() {
        loyaltyProgress.visibility = View.VISIBLE
    }


    override fun showUpdateLoyaltyFields() {
        editLoyaltyNumber.visibility = View.GONE
        deleteInput.visibility = View.VISIBLE
        updateView.visibility = View.VISIBLE

        loyaltyCardNumber.isEnabled = true
        deleteInput.setOnClickListener { loyaltyCardNumber.text.clear() }


        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(loyaltyCardNumber, InputMethodManager.SHOW_FORCED)

        loyaltyProgram?.let {
            updateLoyaltyButton.setOnClickListener { v ->
                val newNumber = loyaltyCardNumber.text
                val fieldNameToValue = mutableMapOf<String, Pair<RequiredField, String>>()
                val type = loyaltyProgram?.requiredFields ?: return@setOnClickListener
                val name = loyaltyProgram?.chainName.toString()
                fieldNameToValue[name] = Pair(type, newNumber.toString())
                val data = fieldNameToValue.map {
                    Triple(it.key, it.value.first, it.value.second)
                }
                presenter.userUpdatedLoyaltyCard(it, data)
            }
        }


        deleteLoyalty.setOnClickListener {
            AlertDialog
                    .Builder(context, R.style.CUSTOM_ALERT)
                    .setMessage(getString(R.string.loyalty_program_delete))
                    .setPositiveButton("Ok") { _, _ ->
                        presenter.deleteLoyaltyProgram()
                    }
                    .setNegativeButton("Cancel") {_, _ ->

                    }
                    .setCancelable(false)
                    .show()
        }
    }


    override fun updateFailure(failure: String?) {
        failure ?: return
        showAlert(failure)
    }

    override fun updateFailure(failure: Int) {

    }

    fun showAlert(failure: String) {
        val failure = failure
        AlertDialog
                .Builder(context, R.style.CUSTOM_ALERT)
                .setMessage(failure)
                .setPositiveButton("Ok") { _, _ ->

                }
                .setCancelable(false)
                .show()

    }


    override fun updateSuccessful() {
        activity?.let {
            Toast.makeText(it, "Loyalty number updated successfully", Toast.LENGTH_SHORT).show()
            it.onBackPressed()
        }
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
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
