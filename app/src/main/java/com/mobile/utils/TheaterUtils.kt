package com.mobile.utils

import android.os.Bundle
import android.support.v4.app.Fragment
import com.mobile.Constants
import com.mobile.fragments.TheaterPolicy
import com.mobile.model.Theater

val Theater.isFlixBrewhouse:Boolean
get() {
    return this.name?.toLowerCase()?.contains("flix brewhouse")==true
}

fun Fragment.showTheaterBottomSheetIfNecessary(theater:Theater?) {
    when(theater?.isFlixBrewhouse) {
        true-> {
            val bundle = Bundle()
            bundle.putString(Constants.POLICY, theater.name)

            val fragobj = TheaterPolicy()
            fragobj.arguments = bundle
            fragobj.show(childFragmentManager, "fr_theaterpolicy")}
    }
}