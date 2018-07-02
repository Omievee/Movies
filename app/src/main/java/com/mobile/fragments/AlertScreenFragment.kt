package com.mobile.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.helpers.LogUtils
import com.mobile.model.Alert
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_alert_screen.*

class AlertScreenFragment : Fragment() {


    internal var myContext: Context? = null
    private var alertObject: Alert? = null

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

        LogUtils.newLog("log>>>", "button?" + alertObject?.dismissButton)

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
        if (alertObject?.dismissButton!! == true) {

            acceptButton.visibility = View.VISIBLE
            acceptButton.text = alertObject?.dismissButtonText

            val acceptURL = alertObject?.dismissButtonWebhook

            acceptButton.setOnClickListener {
                //TODO
                if (Patterns.WEB_URL.matcher(alertObject?.dismissButtonWebhook).matches()) {
                    val alertIntentClick = Intent(Intent.ACTION_VIEW, Uri.parse(alertObject?.dismissButtonWebhook))
                    startActivity(alertIntentClick)
                }
                UserPreferences.setAlertDisplayedId(alertObject!!.id)
                activity?.onBackPressed()
            }
        } else {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        alertObject?.dismissible ?: return
        UserPreferences.setAlertDisplayedId(alertObject?.id)
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
}


