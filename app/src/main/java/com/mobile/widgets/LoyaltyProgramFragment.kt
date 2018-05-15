package com.mobile.widgets

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.widget.TextViewCompat.*
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter
import com.mobile.utils.text.toSentenceCase
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_loyalty_program.*

class LoyaltyProgramFragment : Fragment(), LoyaltyProgramView {

    var adapter: MaterialSpinnerAdapter<TheaterChain>? = null

    var presenter: LoyaltyProgramPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loyalty_program, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        presenter = LoyaltyProgramPresenter(LoyaltyPresentationModel(addLoyaltyProgram = getString(R.string.loyalty_program_add_loyalty_program)), this)
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume()
    }

    override fun showTheaters(theaterChains: List<TheaterChain>) {
        activity?.let {
            adapter = object : MaterialSpinnerAdapter<TheaterChain>(it, theaterChains) {
                override fun getItemText(position: Int): String {
                    return getItem(position).chainName ?: ""
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val view = MaterialSpinnerSpinnerView(activity)
                    view.minimumHeight = resources.getDimension(R.dimen.action_bar_size).toInt()
                    view.bind(getItemText(position))
                    return view
                }
            }
            addLoyaltySpinner.setAdapter(adapter)
            addLoyaltySpinner.setOnItemSelectedListener { view, position, id, item ->
                presenter?.onLoyaltyProgramSelected(position)
                addLoyaltySpinner.text = getString(R.string.loyalty_program_add_loyalty_program)
                addLoyaltySpinner.hint = getString(R.string.loyalty_program_add_loyalty_program)
            }
        }
    }

    override fun showLoyaltyScreenFields(theaterChain: TheaterChain, triple: Map<String, String>?) {
        activity?.let { activity ->
            val fieldNameToValue = mutableMapOf<String, Pair<RequiredField, TextInputEditText>>()
            theaterChain.requiredFields?.forEach { name, type ->
                val inputEditText = TextInputEditText(ContextThemeWrapper(activity, R.style.EditTextStyle)).apply {
                    hint = name.toSentenceCase().toLowerCase()
                    inputType = when (type) {
                        RequiredField.FI_INT -> InputType.TYPE_NUMBER_FLAG_DECIMAL
                        else -> InputType.TYPE_CLASS_TEXT
                    }
                    setText(triple?.get(name))
                    filters = arrayOf(InputFilter.LengthFilter(100))
                }
                setTextAppearance(inputEditText, R.style.LoyaltyEditText)
                fieldNameToValue.put(name, Pair(type, inputEditText))
                val textInputLayout = TextInputLayout(activity).apply {
                    setErrorTextAppearance(R.style.error_appearance)
                    setHintTextAppearance(R.style.text_in_layout_hint_Style)
                    addView(inputEditText)
                }
                val padding = resources.getDimension(R.dimen.margin_standard).toInt()
                loyaltyProgramFieldsLL.apply {
                    removeAllViews()
                    addView(textInputLayout)
                    setPadding(padding, padding, padding, padding)
                }
                loyaltySignInCL.visibility = View.VISIBLE
            }
            loyaltySignInTV.setOnClickListener {
                val data = fieldNameToValue.map {
                    Triple(it.key, it.value.first, it.value.second.text.toString())
                }
                presenter?.onSignInButtonClicked(theaterChain, data)
            }
            loyaltyProgramNameTV.text = theaterChain.chainName
//            AlertDialog.Builder(activity)
//                    .setView(linearLayout)
//                    .setTitle(getString(R.string.loyalty_program_new_title, theaterChain.chainName))
//                    .setPositiveButton(android.R.string.ok, { _, _ ->
//                        val data = fieldNameToValue.map {
//                            Triple(it.key, it.value.first, it.value.second.text.toString())
//                        }
//                        presenter?.onSignInButtonClicked(theaterChain, data)
//                    })
//                    .setNegativeButton(android.R.string.cancel, { _, _ ->
//
//                    })
//                    .show()
        }

    }

    override fun showSpinnerText(text: String?) {
        addLoyaltySpinner.text = text
    }

    override fun showProgress() {
        addLoyaltyProgress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        addLoyaltyProgress.visibility = View.GONE
    }

    override fun showAddLoyaltyError(theaterChain: TheaterChain) {
        activity?.let { activity ->
            AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.loyalty_program_generic_error_title, theaterChain.chainName))
                    .setPositiveButton(android.R.string.ok, { _, _ ->
                        presenter?.retryLoyaltyProgram()
                    }).show()
        }

    }

    override fun showLoyaltyMembership() {

    }

    companion object {
        fun newInstance(): LoyaltyProgramFragment {
            return LoyaltyProgramFragment()
        }
    }
}