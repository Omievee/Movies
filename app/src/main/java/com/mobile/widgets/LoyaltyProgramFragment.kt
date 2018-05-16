package com.mobile.widgets

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
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

    var addLoyaltyAdapter: MaterialSpinnerAdapter<TheaterChain>? = null

    var registerLoyaltyAdapter: RegisteredLoyaltyAdapter? = null

    var presenter: LoyaltyProgramPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loyalty_program, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        presenter = LoyaltyProgramPresenter(LoyaltyPresentationModel(addLoyaltyProgram = getString(R.string.loyalty_program_add_loyalty_program)), this)
    }

    //TODO REMOVE WHEN SWITCHING TO SUPPORT FRAGMENTS
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) {
            presenter = LoyaltyProgramPresenter(LoyaltyPresentationModel(addLoyaltyProgram = getString(R.string.loyalty_program_add_loyalty_program)), this)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume()
    }

    override fun showAddTheaters(theaterChains: List<TheaterChain>) {
        activity?.let {
            addLoyaltySpinner.visibility = View.VISIBLE
            addLoyaltyAdapter = object : MaterialSpinnerAdapter<TheaterChain>(it, theaterChains) {
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
            addLoyaltySpinner.setAdapter(addLoyaltyAdapter)
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
            theaterChain.requiredFields?.forEach { (name, type) ->
                val inputEditText = TextInputEditText(ContextThemeWrapper(activity,R.style.TextInputEditText)).apply {
                    hint = name.toSentenceCase().toLowerCase()
                    inputType = when (type) {
                        RequiredField.FI_INT -> InputType.TYPE_NUMBER_FLAG_DECIMAL
                        else -> InputType.TYPE_CLASS_TEXT
                    }
                    setText(triple?.get(name))
                    filters = arrayOf(InputFilter.LengthFilter(100))
                }
                fieldNameToValue.put(name, Pair(type, inputEditText))
                val textInputLayout = TextInputLayout(ContextThemeWrapper(activity,R.style.TextInputLayout)).apply {
                    addView(inputEditText)
                }
                loyaltyProgramFieldsLL.apply {
                    removeAllViews()
                    addView(textInputLayout)
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
        }

    }

    override fun showAddAMovieTheaterLoyaltyMessage() {
        addLoyaltyProgramDescriptionTV.visibility = View.VISIBLE
    }

    override fun hideAddAMovieTheaterLoyaltyMessage() {
        addLoyaltyProgramDescriptionTV.visibility = View.GONE
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

    override fun showRegisteredTheaters(theaters: List<TheaterChain>) {
        registeredLoyaltyRV.visibility = View.VISIBLE
        myLoyaltyPrograms.visibility = View.VISIBLE
        if(registerLoyaltyAdapter==null) {
            registerLoyaltyAdapter = RegisteredLoyaltyAdapter()
            registeredLoyaltyRV.adapter = registerLoyaltyAdapter
        }
        registerLoyaltyAdapter?.let {
            it.data = RegisteredLoyaltyAdapter.create(it.data, theaters)
        }
    }

    override fun hideAddTheaters() {
        addLoyaltySpinner.visibility = View.GONE
    }

    override fun hideRegisteredTheaters() {
        registeredLoyaltyRV.visibility = View.GONE
        myLoyaltyPrograms.visibility = View.GONE
    }

    override fun hideLoyaltySignIn() {
        loyaltySignInCL.visibility = View.GONE
    }


    companion object {
        fun newInstance(): LoyaltyProgramFragment {
            return LoyaltyProgramFragment()
        }
    }
}