package com.mobile.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.mobile.helpers.LogUtils
import com.mobile.requests.AddressChangeRequest
import com.mobile.session.UserManager
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_change_shipping_address.*
import java.util.*
import javax.inject.Inject

class ChangeShippingAddress : MPFragment() {

    @Inject
    lateinit var userManager: UserManager

    var shippingDisposable: Disposable? = null

    private var firstClick = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_shipping_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        userAddress2.isEnabled = false
        state.isEnabled = false
        zip.isEnabled = false
        city.isEnabled = false

        userAddress.addTextChangedListener(CustomTextWatcher())
        userAddress2.addTextChangedListener(CustomTextWatcher())
        zip.addTextChangedListener(CustomTextWatcher())
        state.addTextChangedListener(CustomTextWatcher())
        city.addTextChangedListener(CustomTextWatcher())

        setData()
        touchListener()
    }

    private fun touchListener(): Boolean {
        userAddress.setOnTouchListener { _, _ ->
            if (firstClick) {

                firstClick = false
                userAddress2.isEnabled = true
                state.isEnabled = true
                zip.isEnabled = true
                city.isEnabled = true

                val typeFilter = AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                        .setCountry("USA")
                        .build()

                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(activity!!)
                    startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2)
                } catch (e: GooglePlayServicesRepairableException) {


                } catch (e: GooglePlayServicesNotAvailableException) {
                    e.printStackTrace()
                }

                return@setOnTouchListener true
            } else {
                return@setOnTouchListener false
            }
        }
        return false
    }

    private fun setData() {
        val address = SpannableStringBuilder(UserPreferences.userInfo.shippingAddressLine1)
        userAddress.text = address



        val addressList = SpannableStringBuilder(UserPreferences.userInfo.shippingAddressLine2)
        val list = Arrays.asList<String>(*addressList.split(",".toRegex()).toTypedArray())
        for (i in list.indices) {

            val cityText = SpannableStringBuilder(list[0].toString().trim { it <= ' ' })
            city.text = cityText
            val stateText = SpannableStringBuilder(list[1].toString().trim { it <= ' ' })
            state.text = stateText
            val zipText = SpannableStringBuilder(list[2].toString().trim { it <= ' ' })
            zip.text = zipText
        }

    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
        shippingDisposable?.dispose()
    }

    private fun saveChanges() {
        saveChanges.setTextColor(resources.getColor(R.color.new_red))
        cancelChanges.setTextColor(resources.getColor(R.color.white))
        saveChanges.setClickable(true)
        saveChanges.setOnClickListener { v ->
            progress.visibility = View.VISIBLE
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
            updateShippingAddress();
        }

        cancelChanges.setOnClickListener { activity?.onBackPressed() }
    }

    private fun updateShippingAddress() {
        val userId = UserPreferences.user.id
        if (userAddress.text.toString() != UserPreferences.userInfo.shippingAddressLine1) {
            if (isValidAddress()) {
                val newAddress = userAddress.text.toString().trim()
                val newAddress2 = userAddress2.text.toString().trim()
                val newCity = city.text.toString().trim()
                val newZip = zip.text.toString().trim()
                val newState = state.text.toString().trim()

                val section = "shippingAddress"
                val request = AddressChangeRequest(newAddress, newAddress2, newCity, newState, newZip, section)

                shippingDisposable?.dispose()
                shippingDisposable = userManager
                        .updateAddress(userId, request)
                        .subscribe({
                            Toast.makeText(context, getString(R.string.profile_shipping_update), Toast.LENGTH_SHORT).show()
                            activity?.onBackPressed()
                        }, { error ->
                            when (error) {
                                is ApiError -> Toast.makeText(context, error.error.message, Toast.LENGTH_SHORT).show()
                                else -> Toast.makeText(context, R.string.generic_error, Toast.LENGTH_LONG).show()
                            }
                        })
            } else {
                progress.visibility = View.GONE
            }
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == -1) {
                userAddress.clearFocus()
                userAddress2.clearFocus()
                state.clearFocus()
                zip.clearFocus()
                city.clearFocus()

                val place = PlaceAutocomplete.getPlace(context, data!!)
                val address = place.address!!.toString()
                val localList = Arrays.asList(*address.split(",".toRegex()).toTypedArray())
                for (i in localList.indices) {
                    if (localList[2].trim { it <= ' ' }.length < 8) {
                        Toast.makeText(context, "Invalid Address", Toast.LENGTH_SHORT).show()
                        firstClick = true
                    } else {
                        userAddress.setText(localList[0])
                        city.setText(localList[1].trim { it <= ' ' })
                        val State = localList[2].substring(0, 3).trim { it <= ' ' }
                        val zipString = localList[2].substring(4, 9)
                        state.setText(State)
                        zip.setText(zipString)
                    }
                }
                userAddress.clearFocus()
                userAddress2.clearFocus()
                state.clearFocus()
                zip.clearFocus()
                city.clearFocus()
                saveChanges()
            }
        }
    }


    private fun isValidAddress(): Boolean {
        address1TextInputLayout.error = null
        cityTextInputLayout.error = null
        stateTextInputLayout.error = null
        zipTextInputLayout.error = null

        var i = 0
        if (!userAddress.text.toString().trim { it <= ' ' }.isEmpty() && !city.text.toString().trim { it <= ' ' }.isEmpty() && !zip.text.toString().trim { it <= ' ' }.isEmpty() && !state.text.toString().trim { it <= ' ' }.isEmpty()) {

            //Validating Address
            val address1Array = userAddress.text.toString().split("\\W+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (address1Array.size >= 2 && address1Array[0].trim { it <= ' ' }.matches(".*\\d+.*".toRegex())) {
                i++
            } else {
                address1TextInputLayout.error = resources.getString(R.string.address_invalid_address)
                userAddress.clearFocus()
                LogUtils.newLog("ADDRESS", "isValidAddress: ")
            }

            //Validating City
            val cityArray = city.text.toString().split("\\W+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val cityWithNotWhiteSpaces = city.text.toString().replace("\\s+".toRegex(), "")
            //If city has less than 3 words
            if (cityArray.size <= 3 && cityWithNotWhiteSpaces.matches("^[a-zA-Z]+$".toRegex())) {
                i++
            } else {
                cityTextInputLayout.error = resources.getString(R.string.address_invalid_city)
                city.clearFocus()
            }

            //Validating State
            if (state.text.toString().trim { it <= ' ' }.length == 2 && state.text.toString().trim { it <= ' ' }.matches("^[a-zA-Z]+$".toRegex())) {
                i++
            } else {
                stateTextInputLayout.error = resources.getString(R.string.address_invalid_state)
                state.clearFocus()
            }

            //Validating Zip Code
            if (zip.text.toString().trim { it <= ' ' }.matches("^[0-9]+$".toRegex()) && zip.text.toString().trim { it <= ' ' }.length >= 5) {
                i++
            } else {
                zipTextInputLayout.error = resources.getString(R.string.address_invalid_zip)
                zip.clearFocus()
            }


        } else {
            if (userAddress.text.toString().trim { it <= ' ' }.isEmpty()) {
                address1TextInputLayout.error = resources.getString(R.string.address_empty_shipping_address)
                userAddress.clearFocus()
            }
            if (state.text.toString().trim { it <= ' ' }.isEmpty()) {
                stateTextInputLayout.error = resources.getString(R.string.address_empty_state)
                state.clearFocus()
            }
            if (zip.text.toString().trim { it <= ' ' }.isEmpty()) {
                zipTextInputLayout.error = resources.getString(R.string.address_empty_zip)
                zip.clearFocus()
            }
            if (city.text.toString().trim { it <= ' ' }.isEmpty()) {
                cityTextInputLayout.error = resources.getString(R.string.address_empty_city)
                city.clearFocus()
            }
        }
        return i == 4
    }


    inner class CustomTextWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {

            if (userAddress.isFocused || userAddress2.isFocused || city.isFocused || state.isFocused || zip.isFocused) {
                saveChanges()
            }
            if (userAddress.hasFocus())
                address1TextInputLayout.error = null
            if (city.hasFocus())
                cityTextInputLayout.error = null
            if (state.hasFocus())
                stateTextInputLayout.error = null
            if (zip.hasFocus())
                zipTextInputLayout.error = null
        }
    }
}
