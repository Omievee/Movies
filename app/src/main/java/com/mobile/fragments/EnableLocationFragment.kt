package com.mobile.fragments


import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants
import com.mobile.location.LocationManager

import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fr_enablelocation.*
import javax.inject.Inject


class EnableLocationFragment : BottomSheetDialogFragment() {

    @Inject lateinit var locationProvider:LocationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_enablelocation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        enableButton.setOnClickListener { _ ->
            startActivityForResult(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), Constants.ENABLE_LOCATION_CODE)
        }
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        if(locationProvider.isLocationEnabled()) {
            dismiss()
        }
    }

    companion object {

        fun newInstance(): EnableLocationFragment {
            return EnableLocationFragment()
        }
    }
}
