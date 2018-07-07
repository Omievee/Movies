package com.mobile.reservation

import com.mobile.location.LocationManager
import com.mobile.network.Api
import com.mobile.tickets.TicketManager
import dagger.Module
import dagger.Provides

@Module
class ReservationCheckinModule {

    @Provides
    fun providePresenter(fragment:CheckInFragment, api:TicketManager, locationManager: LocationManager) : CheckInFragmentPresenter {
        return CheckInFragmentPresenter(fragment, api, locationManager)
    }

}