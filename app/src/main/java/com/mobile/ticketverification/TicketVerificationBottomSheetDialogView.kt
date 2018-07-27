package com.mobile.ticketverification

interface TicketVerificationBottomSheetDialogView {
    fun requestPermissionsIfNecessary()
    fun showCameraRequiredDialog()
    fun showCameraRequiredDialogPermanently()
    fun showOcrCaptureFragment()
    fun updateDebugView(data: VerificationData)
    fun setTitle(title: String?)
}