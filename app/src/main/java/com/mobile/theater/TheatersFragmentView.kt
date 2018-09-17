package com.mobile.theater

import com.mobile.fragments.LocationRequiredView
import com.mobile.location.UserLocation
import com.mobile.model.Theater

interface TheatersFragmentView : LocationRequiredView {

    fun setAdapterData(location: UserLocation, theaters: List<Theater>)
    fun showMap(invisible:Boolean=false)
    fun showNoTheatersFound()
    fun hideNoTheatersFound()
    fun scrollToTop()
    fun showNoLocationFound()
    fun clearSearch()

}