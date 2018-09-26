package com.mobile.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.moviepass.R

class AccountInformation : MPFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setData()
        return inflater.inflate(R.layout.fragment_account_information, container, false)
    }

    private fun setData() {

    }

}
