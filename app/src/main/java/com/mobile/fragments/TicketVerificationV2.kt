package com.mobile.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.model.ObjectMetadata
import com.helpshift.support.ApiConfig
import com.helpshift.support.Metadata
import com.helpshift.support.Support
import com.helpshift.util.HelpshiftConnectionUtil.isOnline
import com.mobile.Constants
import com.mobile.UserPreferences
import com.mobile.activities.TicketVerification_NoStub
import com.mobile.application.Application
import com.mobile.helpers.LogUtils
import com.mobile.model.PopInfo
import com.mobile.network.RestClient
import com.mobile.requests.VerificationRequest
import com.mobile.reservation.ReservationActivity
import com.mobile.responses.VerificationResponse
import com.mobile.tv.TicketVerificationView
import com.mobile.utils.AppUtils
import com.mobile.utils.onBackExtension
import com.moviepass.BuildConfig
import com.moviepass.R
import kotlinx.android.synthetic.main.activity_reservation.*
import kotlinx.android.synthetic.main.fragment_ticket_verification_v2.*
import kotlinx.android.synthetic.main.layout_current_reservation.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


private const val POP_INFO = "pop_info"
private const val TICKET_STATUS = "redeem"


class TicketVerificationV2 : MPFragment() {

    var thisActivity: Activity? = null
    private var popInfo: PopInfo? = null
    private var isTicketRedeemed: Boolean? = null
    private val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    var photoFile: File? = null
    val APP_TAG = "TicketVerification"
    val photoFileName = "TicketVerification.jpg"
    var bmOptions: BitmapFactory.Options? = null
    var key: String? = null
    var objectMetadata: ObjectMetadata? = null
    var transferUtility: TransferUtility? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            popInfo = it.getParcelable<PopInfo>(POP_INFO)
            isTicketRedeemed = it.getBoolean(TICKET_STATUS)
        }
    }

    private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ticket_verification_v2, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popInfo?.let {
            ticketVerificationV.bind(it, isTicketRedeemed ?: false)
        }

        transferUtility = TransferUtility.builder()
                .context(activity?.applicationContext)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client((activity?.applicationContext as Application).amazonS3Client)
                .build()

        ticketVerificationV.setOnClickListeners(object : TicketVerificationView.ClickListeners {
            override fun close() {
                activity?.onBackPressed()
            }

            override fun getHelp() {
                openHelpshift()
            }

            override fun takePicture() {
                openCamera()
            }

            override fun noTicketSub() {
                val intent = Intent(activity, TicketVerification_NoStub::class.java)
                val res = popInfo?.reservationId
                intent.putExtra(Constants.SCREENING, res)
                startActivity(intent)
            }

        })

    }

    private fun openCamera() {
        context?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(CAMERA_PERMISSIONS, Constants.REQUEST_CAMERA_CODE)
            } else {
                scanTicket()
            }
        }
    }

    private fun scanTicket() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFileUri(photoFileName)
        context?.let {
            if (photoFile != null) {
                val fileProvider = FileProvider.getUriForFile(it, resources.getString(R.string.authority_file_provider), photoFile
                        ?: getPhotoFileUri(photoFileName))
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            }
        }

        if (intent.resolveActivity(context?.packageManager) != null) {
            LogUtils.newLog(Constants.TAG, "scanTicket: ")
            startActivityForResult(intent, Constants.REQUEST_CAMERA_CODE)
        }
    }

    private fun getPhotoFileUri(photoFileName: String): File {
        val mediaStorageDir = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG)

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            LogUtils.newLog(APP_TAG, "failed to create directory")
        }

        return File(mediaStorageDir.getPath() + File.separator + photoFileName)
    }

    fun openHelpshift() {
        Support.showFAQSection(activity, Constants.TICKET_VERIFICATION_FAQ_SECTION)
    }

    override fun onBack(): Boolean {
        return when (isTicketRedeemed) {
            true -> true
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {

            if (photoFile != null) {
                bmOptions = BitmapFactory.Options()

                Log.d(Constants.TAG, "onActivityResult: $bmOptions")
                BitmapFactory.decodeFile(photoFile?.getAbsolutePath(), bmOptions)
                val photoW = bmOptions?.outWidth
                val photoH = bmOptions?.outHeight

                val scaleFactor = photoW?.div(1024)?.let { photoH?.div(1024)?.let { it1 -> Math.min(it, it1) } }
                if (scaleFactor != 1) {
                    bmOptions?.inSampleSize = scaleFactor
                }
                bmOptions?.inJustDecodeBounds = false
                val image = BitmapFactory.decodeFile(photoFile?.getAbsolutePath(), bmOptions)
                val bos = ByteArrayOutputStream()
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(photoFile)
                    LogUtils.newLog("compressing file " + photoFile?.getAbsolutePath())
                    image.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                } catch (ignored: Exception) {

                } finally {
                    if (fos != null) {
                        try {
                            fos.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                    image.recycle()
                }


            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(STORAGE_PERMISSIONS, Constants.REQUEST_STORAGE_CODE)
                } else {
                    createFileForUpload()
                }
            } else {
                Log.d(Constants.TAG, "onActivityResult: $bmOptions")
                createFileForUpload()
            }
        }
    }

    fun createFileForUpload() {
        val handler = Handler()
        //TODO
        ticketVerificationV.enableLoading()
//        ticketScan.setVisibility(View.INVISIBLE)

        handler.postDelayed({

            val bos = ByteArrayOutputStream()

            val bitmapdata = bos.toByteArray()

            val pictureFile = getOutputMediaFile()
            if (pictureFile == null) {
                return@postDelayed
            }
            try {
                val fos = FileOutputStream(pictureFile!!)
                fos.write(bitmapdata)
                fos.close()
            } catch (e: FileNotFoundException) {
                LogUtils.newLog(Constants.TAG, "File not found: " + e.message)
            } catch (e: IOException) {

                LogUtils.newLog(Constants.TAG, "Error accessing file: " + e.message)

            }

            //Turn into file
            val getPictureFile = getOutputMediaFile()
            if (getPictureFile == null) {
                Toast.makeText(activity, "Failed to create File", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }
            uploadToAWS(getPictureFile)
        }, 4000)
    }


    private fun uploadToAWS(ticketPhoto: File) {
        objectMetadata = ObjectMetadata()
        key = popInfo?.reservationId.toString()
        val reservationId = popInfo?.reservationId
        val showTime = popInfo?.showtime
        val movieTitle = popInfo?.movieTitle
        val theaterName = popInfo?.theaterName
        val reservationKind = "reskind"
        val tribuneMovieId = popInfo?.tribuneMovieId
        val tribuneTheaterId = popInfo?.tribuneTheaterId
        objectMetadata?.userMetadata = showTime?.let { tribuneMovieId?.let { it1 -> movieTitle?.let { it2 -> tribuneTheaterId?.let { it3 -> theaterName?.let { it4 -> metaDataMap(reservationId.toString(), it, it1, it2, it3, it4, reservationKind) } } } } }


        val observer = transferUtility?.upload(BuildConfig.BUCKET, key, ticketPhoto, objectMetadata)
        observer?.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    val ticketVerificationRequest = VerificationRequest()
                    RestClient.getAuthenticated().verifyTicket(reservationId
                            ?: 0, ticketVerificationRequest).enqueue(object : Callback<VerificationResponse> {
                        override fun onResponse(call: Call<VerificationResponse>, response: Response<VerificationResponse>) {
                            if (response != null && response.isSuccessful) {
                                //TODO
                                ticketVerificationV.disableLoading()
                                Toast.makeText(activity, "Your ticket stub has been submitted", Toast.LENGTH_LONG).show()
                                pictureSubmitted()
                            } else {
                                ticketVerificationV.disableLoading()
                                var jObjError: JSONObject? = null
                                try {
                                    jObjError = JSONObject(response.errorBody()?.string())
                                    if (jObjError.getString("message") == "Verification status is different from PENDING_SUBMISSION") {
                                        //TODO
                                        ticketVerificationV.disableLoading()
                                        Toast.makeText(activity, "Your ticket stub has been submitted", Toast.LENGTH_LONG).show()
                                        pictureSubmitted()
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                            }
                        }

                        override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                            ticketVerificationV.disableLoading()
                            Toast.makeText(activity, "Server Error. Try Again", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

            }

            override fun onError(id: Int, ex: Exception) {
                LogUtils.newLog(Constants.TAG, "onError: ")
            }
        })
    }

    fun pictureSubmitted(){
        when (isTicketRedeemed){
            true -> {
                isTicketRedeemed = false
                activity?.onBackPressed()
            }
            false -> activity?.finish()
        }
    }

    private fun metaDataMap(reservationId: String, showTime: String, movieId: String, movieTitle: String,
                            theaterId: String, theaterName: String, reservationKind: String): HashMap<String, String> {
        val meta = HashMap<String, String>()
        meta["reservation_id"] = reservationId//reservationId
        meta["showtime"] = showTime//ShowTime
        meta["movie_id"] = movieId//Movie Id
        meta["movie_title"] = movieTitle// MovieTitle
        meta["theater_id"] = theaterId//TheaterId
        meta["theater_name"] = theaterName//TheaterName
        meta["reservation_kind"] = reservationKind//reservationKind
        meta["device_name"] = AppUtils.getDeviceName()//Device Name
        meta["os_version"] = AppUtils.getOsCodename()//OS VERSION
        meta["user_id"] = UserPreferences.getUserId().toString()//UserId
        meta["version_code"] = BuildConfig.VERSION_CODE.toString()
        meta["version_name"] = BuildConfig.VERSION_NAME
        meta["os"] = "android"

        return meta
    }

    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MoviePass")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogUtils.newLog("MoviePass", "failed to create directory")
                return null
            }
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile: File
        mediaFile = File(mediaStorageDir.path + File.separator + timeStamp + ".jpg")
        return mediaFile
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        thisActivity = activity
    }

    companion object {
        fun newInstance(param1: PopInfo, isTicketRedeemed: Boolean): TicketVerificationV2 {
            return TicketVerificationV2().apply {
                arguments = Bundle().apply {
                    putParcelable(POP_INFO, param1)
                    putBoolean(TICKET_STATUS, isTicketRedeemed)
                }
            }
        }
    }
}
