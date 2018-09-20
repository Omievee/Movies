package com.mobile.ticketverification

import android.Manifest.permission.*
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager.*
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.Constants
import com.mobile.reservation.CurrentReservationV2
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_ticket_verification.*
import javax.inject.Inject
import android.content.Intent
import android.widget.TextView
import com.mobile.utils.replaceFragmentExtension
import com.mobile.utils.startCameraIntent
import com.mobile.widgets.MPAlertDialog
import com.moviepass.BuildConfig


class TicketVerificationBottomSheetDialogFragment : BottomSheetDialogFragment(), TicketVerificationBottomSheetDialogView {

    override fun showOcrCaptureFragment() {
        replaceFragmentExtension(OcrCaptureFragment())
        debugView?.bringToFront()
    }

    private var hasCameraPermission: Boolean = false
    get() {
        val context = activity ?: return false
        return checkSelfPermission(context, CAMERA) == PERMISSION_GRANTED
    }

    private var canRequestCameraPermission:Boolean = true
    get() {
        val context = activity ?: return false
        return ActivityCompat.shouldShowRequestPermissionRationale(context, CAMERA)
    }

    var didReceiveCameraPermissionResultAsTrueForOnResume = false
    var didReceiveCameraPermissionResultAsFalseForOnResume = false

    override fun requestPermissionsIfNecessary() {
        when (hasCameraPermission) {
            true -> presenter.onCameraPermission()
            false -> requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(CAMERA), Constants.REQUEST_CAMERA_CODE_FOR_TICKET_VERIFICATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != Constants.REQUEST_CAMERA_CODE_FOR_TICKET_VERIFICATION) {
            return
        }
        handleCameraPermissionResult()
    }

    private fun handleCameraPermissionResult() {
        when (hasCameraPermission) {
            true -> didReceiveCameraPermissionResultAsTrueForOnResume = true
            false -> didReceiveCameraPermissionResultAsFalseForOnResume = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode!= Constants.REQUEST_CAMERA_CODE_FOR_TICKET_VERIFICATION_DENIED) {
            return
        }
        handleCameraPermissionResult()
    }

    override fun onResume() {
        super.onResume()
        when (didReceiveCameraPermissionResultAsTrueForOnResume) {
            true -> {
                didReceiveCameraPermissionResultAsTrueForOnResume = false
                presenter.onCameraPermission()
            }
        }
        when(didReceiveCameraPermissionResultAsFalseForOnResume) {
            true -> {
                when(canRequestCameraPermission) {
                    true-> presenter.onDeclinedCameraPermission()
                    false-> presenter.onDeclinedCameraPermissionPermanently()
                }
            }
        }
    }

    @Inject
    lateinit var presenter: TicketVerificationBottomSheetDialogFragmentPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ticket_verification, container, false)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun setTitle(title: String?) {
        this.title.text = title
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraFL
                .setOnClickListener {
                    presenter.onCameraClick()
                }
        presenter.onViewCreated()
        if(BuildConfig.DEBUG) {
            showDebug(view)
        }
    }

    private fun showDebug(view:View) {
        view.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun updateDebugView(data: VerificationData) {
        val str = data.toString()
        debugView?.text = str
    }

    override fun showCameraRequiredDialog() {
        val context = context?:return
        MPAlertDialog(context)
                .setTitle(R.string.ticket_verification_camera_dialog_title)
                .setNegativeButton(R.string.not_now, null)
                .setPositiveButton(R.string.try_again) { _, _ ->
                    presenter.onCameraClick()
                }.show()
    }

    override fun showCameraRequiredDialogPermanently() {
        val context = context?:return
        MPAlertDialog(context)
                .setTitle(R.string.ticket_verification_camera_dialog_title)
                .setMessage(R.string.ticket_verification_camera_dialog_message)
                .setNegativeButton(R.string.not_now) { _, _ ->
                    presenter.onUserRefusesToEnableCamera()
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    launchCameraPermissionIntent()
                }.show()
    }

    private fun launchCameraPermissionIntent() {
        startCameraIntent(Constants.REQUEST_CAMERA_CODE_FOR_TICKET_VERIFICATION_DENIED)
    }
}

const val RESERVATION = "reservation"

fun newInstance(reservationV2: CurrentReservationV2):TicketVerificationBottomSheetDialogFragment {
    val f = TicketVerificationBottomSheetDialogFragment()
    f.arguments = Bundle().apply {
        putParcelable("reservation", reservationV2)
    }
    return f
}