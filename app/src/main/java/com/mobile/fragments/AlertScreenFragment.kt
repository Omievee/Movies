package com.mobile.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.model.Alert
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_alert_screen.*
import okhttp3.*
import java.io.IOException


class AlertScreenFragment : MPFragment() {

    private var alertObject: Alert? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertObject = arguments?.getParcelable("alert")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_alert_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertTitle.text = alertObject?.title
        alertBody.text = alertObject?.body

        val showButton = alertObject?.dismissButton ?: return
        val dismiss = alertObject?.dismissible ?: return

        if (dismiss) {
            dismissAlert.setOnClickListener { _ ->
                UserPreferences.alertDisplayedId = alertObject?.id
                activity?.onBackPressed()
            }
        } else {
            dismissAlert.visibility = View.INVISIBLE
        }

        if (alertObject?.urlTitle.isNullOrEmpty() || alertObject?.url.isNullOrEmpty()) {
            alertClickMessage.visibility = View.GONE
        } else {
            alertClickMessage.visibility = View.VISIBLE
            linkText.text = alertObject?.urlTitle
            alertClickMessage.setOnClickListener { _ ->
                if (Patterns.WEB_URL.matcher(alertObject?.url).matches()) {
                    val alertIntentClick = Intent(Intent.ACTION_VIEW, Uri.parse(alertObject?.url))
                    startActivity(alertIntentClick)
                }
            }
        }
        if (showButton) {
            acceptButton.visibility = View.VISIBLE
            acceptButton.text = alertObject?.dismissButtonText
            val acceptURL = alertObject?.dismissButtonWebhook + "&userId=" + UserPreferences.userId
            acceptButton.setOnClickListener {
                run(acceptURL)
                UserPreferences.alertDisplayedId = alertObject?.id
                alertObject?.dismissible = true
                activity?.onBackPressed()
            }
        } else {
            acceptButton.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertObject?.dismissible == true) {
            UserPreferences.alertDisplayedId = alertObject?.id
        }

    }

    companion object {

        fun newInstance(alert: Alert): AlertScreenFragment {
            val fragment = AlertScreenFragment()
            val args = Bundle()
            args.putParcelable("alert", alert)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBack(): Boolean {
        return alertObject?.dismissible == false
    }

    @Throws(IOException::class)
    fun run(url: String): String {

        val client = OkHttpClient()

        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("RESPONSE", ">>>>>>>>>>> FAIL")
            }

            override fun onResponse(call: Call?, response: Response?) {
                Log.d("RESPONSE", ">>>>>>>>>>> PASS")
            }

            @Throws(IOException::class)
            fun onResponse(response: Response) {
            }
        });
        return response.toString()
    }
}


