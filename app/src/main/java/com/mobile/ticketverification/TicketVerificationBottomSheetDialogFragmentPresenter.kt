package com.mobile.ticketverification

import com.mobile.reservation.CurrentReservationV2
import io.reactivex.disposables.Disposable

class TicketVerificationBottomSheetDialogFragmentPresenter(
        val currentReservationV2: CurrentReservationV2,
        val view:TicketVerificationBottomSheetDialogView,
        val detectedTextManager: DetectedTextManager,
        val barcodeDetectorManager: BarcodeDetectorManager
        ) {

    var disposable:Disposable? = null
    var barcodeDiosposable:Disposable? = null

    var data = VerificationData()

    fun onViewCreated() {
        disposable = detectedTextManager
                .payload()
                .map { text->
                    data.update(currentReservationV2, text)
                    text
                }
                .subscribe({text->
                    view.updateDebugView(data)
                },{
                    it.printStackTrace()
                })
        barcodeDiosposable = barcodeDetectorManager
                .payload()
                .subscribe({
                    data.update(it)
                },{
                    it.printStackTrace()
                })
        view.setTitle(currentReservationV2.title)
    }

    fun onDestroy() {
        disposable?.dispose()
    }

    fun onCameraClick() {
        view.requestPermissionsIfNecessary()
    }

    fun onCameraPermission() {
        view.showOcrCaptureFragment()
    }

    fun onDeclinedCameraPermission() {
        view.showCameraRequiredDialog()
    }

    fun onDeclinedCameraPermissionPermanently() {
        view.showCameraRequiredDialogPermanently()
    }

    fun onUserRefusesToEnableCamera() {

    }

}
