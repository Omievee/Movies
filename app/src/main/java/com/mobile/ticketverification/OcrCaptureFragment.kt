package com.mobile.ticketverification

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.moviepass.R
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.fragment_ocr_capture.*
import android.widget.Toast
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.mobile.Constants
import com.mobile.camera.*
import dagger.android.support.AndroidSupportInjection
import java.io.IOException
import javax.inject.Inject
import com.google.android.gms.vision.MultiProcessor
import java.util.*

class OcrCaptureFragment : Fragment() {

    @Inject
    lateinit var manager:DetectedTextManager

    @Inject
    lateinit var barcodeManager:BarcodeDetectorManager

    var cameraSource: CameraSource? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ocr_capture, container, false)
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createCameraSource()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        preview?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        preview?.release()
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val activity = activity ?: return
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                activity)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, Constants.REQUEST_GMS_CAMERA_CODE)
            dlg.show()
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    private fun createCameraSource() {
        val activity = activity ?: return
        val textRecognizer = TextRecognizer.Builder(context).build()
        val overlay = graphicOverlay as GraphicOverlay
        textRecognizer.setProcessor(OcrDetectorProcessor(overlay, manager))

        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(barcodeManager, overlay as GraphicOverlay<BarcodeGraphic>, activity)

        barcodeDetector.setProcessor(
                MultiProcessor.Builder(barcodeFactory).build())

        if (!textRecognizer.isOperational) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null

            if (hasLowStorage) {
                Toast.makeText(activity, R.string.low_storage_error, Toast.LENGTH_LONG).show()
            }
        }
        cameraSource = CameraSource.Builder(activity, Arrays.asList(barcodeDetector) as List<Detector<*>>?)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                .setRequestedFps(12.0f)
                .build()
    }

}