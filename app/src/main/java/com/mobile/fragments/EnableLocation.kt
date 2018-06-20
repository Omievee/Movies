package com.mobile.fragments


import android.content.Intent

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants

import com.moviepass.R
import kotlinx.android.synthetic.main.fr_enablelocation.*


class EnableLocation : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_enablelocation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        enableButton.setOnClickListener { _ ->
            startActivityForResult(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), Constants.ENABLE_LOCATION_CODE)
            dismiss()
        }
    }

    companion object {

        fun newInstance(): EnableLocation {
            return EnableLocation()
        }
    }
}
