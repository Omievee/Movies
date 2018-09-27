package com.mobile.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mobile.ApiError
import com.mobile.Constants
import com.mobile.home.HomeActivity
import com.mobile.model.Screening
import com.mobile.network.Api
import com.mobile.network.RestClient
import com.mobile.requests.CardActivationRequest
import com.moviepass.R
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ac_activate_movie_pass_card.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class ActivateMoviePassCard : AppCompatActivity() {


    @Inject
    internal lateinit var api: Api

    internal var activateCardDisposable: Disposable? = null

    internal lateinit var progress: View
    internal lateinit var activateInstructions: TextView
    internal lateinit var activateManualInput: TextView
    internal lateinit var activateDigits: EditText
    internal lateinit var activateScanCardIcon: ImageView
    internal lateinit var activateXOut: ImageView
    internal lateinit var digits: String
    var screeningObject: Screening? = null
    var selectedShowTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_activate_movie_pass_card)


        activateManualInput = findViewById(R.id.ACTIVATECARD_MANULINPUT)
        activateDigits = findViewById(R.id.ACTIVATE_DIGITS)
        activateScanCardIcon = findViewById(R.id.ACTIVATECARD_SCAN_ICON)
        activateXOut = findViewById(R.id.ACTIVATECARD_X_OUT)
        progress = findViewById(R.id.progress)
        activateScanCardIcon.setOnClickListener { _ -> scanCard() }


        api = RestClient.getAuthenticated()

        val intent = intent ?: return
        screeningObject = intent.getParcelableExtra(Constants.SCREENING)
        selectedShowTime = getIntent().getStringExtra(Constants.SHOWTIME)


        activateXOut.setOnClickListener { _ ->
            finish()
        }

        activateManualInput.setOnClickListener { _ ->
            activatecard_instructions.text = getString(R.string.last_4)
            header1.visibility = View.GONE
            instruct1.visibility = View.GONE
            instruct2.visibility = View.GONE

            fadeOut(activateScanCardIcon)
            activateScanCardIcon.visibility = View.GONE
            fadeOut(activateManualInput)
            activateManualInput.visibility = View.GONE

            fadeIn(activateButton)
            activateButton.visibility = View.VISIBLE
            fadeIn(activateDigits)
            activateDigits.visibility = View.VISIBLE
        }

        activateButton.setOnClickListener { _: View ->
            progress.visibility = View.VISIBLE
            continueActivation()
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun scanCard() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_CODE)
            val scanIntent = Intent(this@ActivateMoviePassCard, CardIOActivity::class.java)
            scanIntent.putExtra(CardIOActivity.EXTRA_UNBLUR_DIGITS, 4)
            startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE)
        } else {
            val scanIntent = Intent(this@ActivateMoviePassCard, CardIOActivity::class.java)
            scanIntent.putExtra(CardIOActivity.EXTRA_UNBLUR_DIGITS, 4)
            startActivityForResult(scanIntent, Constants.CARD_SCAN_REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == Constants.CARD_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                val scanResult = data.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)

                activateScanCardIcon.visibility = View.GONE
                activateManualInput.visibility = View.GONE
                activateButton.visibility = View.VISIBLE
                activateDigits.visibility = View.VISIBLE
                activateDigits.setText(scanResult.lastFourDigitsOfCardNumber)
                header1.visibility = View.GONE
                instruct1.visibility = View.GONE
                instruct2.visibility = View.GONE
                activatecard_instructions.text = getString(R.string.last_4)

                activateButton.setOnClickListener { _ -> continueActivation() }

            }
        }
    }

    private fun continueActivation() {

        digits = activateDigits.text.toString().trim { it <= ' ' }
        val request = CardActivationRequest(digits)

        activateCardDisposable?.dispose()

        activateCardDisposable = api
                .activateCardRX(request)
                .subscribe({ _ ->
                    progress.visibility = View.GONE
                    val activate = Intent(this@ActivateMoviePassCard, AutoActivatedCard::class.java)
                    activate.putExtra(Constants.SCREENING, screeningObject)
                    activate.putExtra(Constants.SHOWTIME, selectedShowTime)
                    startActivity(activate)
                    finish()

                }
                ) { error ->
                    progress.visibility = View.GONE
                    if (error is ApiError) {
                        Toast.makeText(this@ActivateMoviePassCard, error.error.message, Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun fadeIn(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = 1000

        val animation = AnimationSet(false) //change to false
        animation.addAnimation(fadeIn)
        view.animation = animation

    }

    fun fadeOut(view: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = DecelerateInterpolator() //add this
        fadeOut.duration = 1000

        val animation = AnimationSet(false) //change to false
        animation.addAnimation(fadeOut)
        view.animation = animation
    }


    override fun onDestroy() {
        super.onDestroy()
        activateCardDisposable = null
    }

    companion object {
        private val REQUEST_CAMERA_CODE = 201
        private val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }



}
