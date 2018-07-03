package com.mobile.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.mobile.UserPreferences
import com.mobile.model.Alert
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_alert_screen.*


class AlertScreenFragment : MPFragment() {


    internal var myContext: Context? = null
    private var alertObject: Alert? = null
    var acceptedClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertObject = arguments!!.getParcelable("alert")
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
            dismissAlert.setOnClickListener { v ->
                UserPreferences.setAlertDisplayedId(alertObject!!.id)
                activity?.onBackPressed()
            }
        } else {
            dismissAlert.visibility = View.INVISIBLE
        }

        if (TextUtils.isEmpty(alertObject?.urlTitle) || TextUtils.isEmpty(alertObject?.url)) {
            alertClickMessage.visibility = View.INVISIBLE

        } else {
            LinkText.text = alertObject?.urlTitle
            alertClickMessage.setOnClickListener { v ->
                if (Patterns.WEB_URL.matcher(alertObject?.url).matches()) {
                    val alertIntentClick = Intent(Intent.ACTION_VIEW, Uri.parse(alertObject!!.url))
                    startActivity(alertIntentClick)
                }
            }
        }
        if (showButton) {
            acceptButton.visibility = View.VISIBLE
            acceptButton.text = alertObject?.dismissButtonText

            val acceptURL = alertObject?.dismissButtonWebhook + "&userId=" + UserPreferences.getUserId()
            acceptButton.setOnClickListener {
                //TODO
                val client = AsyncHttpClient()
                client.get(acceptURL, object : AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<cz.msebera.android.httpclient.Header>, responseBody: ByteArray) {
                    }

                    override fun onFailure(statusCode: Int, headers: Array<cz.msebera.android.httpclient.Header>, responseBody: ByteArray, error: Throwable) {
                    }
                })
                UserPreferences.setAlertDisplayedId(alertObject?.id)
                alertObject?.dismissible = true
                activity?.onBackPressed()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertObject?.dismissible!!) {
            UserPreferences.setAlertDisplayedId(alertObject?.id)
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

        return !alertObject?.dismissible!!
    }
}


