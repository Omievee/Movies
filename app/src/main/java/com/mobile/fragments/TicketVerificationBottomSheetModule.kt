package com.mobile.fragments

import com.mobile.di.FragmentScope
import com.mobile.network.Api
import com.mobile.network.MicroApi
import com.mobile.reservation.CurrentReservationV2
import com.mobile.session.SessionManager
import com.mobile.ticketverification.*
import dagger.Module
import dagger.Provides

@Module()
class TicketVerificationBottomSheetModule {

    init {
        println("OK")
    }

    @Provides
    @TVScope
    fun providePresenter(fragment: TicketVerificationBottomSheetDialogFragment,
                         api: Api, micro: MicroApi,
                         sessionManager: SessionManager,
                         detectedTextManager: DetectedTextManager,
                         barcodeDetectorManager: BarcodeDetectorManager
    ):
            TicketVerificationBottomSheetDialogFragmentPresenter {
        return TicketVerificationBottomSheetDialogFragmentPresenter(
                fragment.arguments?.getParcelable(RESERVATION) ?: CurrentReservationV2(),
                fragment,
                detectedTextManager,
                barcodeDetectorManager)
    }

}