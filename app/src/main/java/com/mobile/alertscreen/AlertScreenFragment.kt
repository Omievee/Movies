package com.mobile.alertscreen

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.UserPreferences
import com.mobile.fragments.MPFragment
import com.mobile.model.Alert
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fr_alert_screen.*
import javax.inject.Inject


class AlertScreenFragment : MPFragment(), AlertScreenView {


    lateinit var alertObject: Alert

    @Inject
    lateinit var presenter: AlertScreenPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertObject = arguments?.getParcelable(ALERT) ?: return
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_alert_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onCreateView(alertObject)
    }


    override fun setAlert(alert: Alert) {
        alertTitle.text = alert.title
        alertBody.text = alert.body
    }

    override fun hideDismissIcon() {
        dismissAlert.visibility = View.INVISIBLE
    }

    override fun dismissAlertScreen(id: String?) {
        dismissAlert.setOnClickListener { _ ->
            UserPreferences.alertDisplayedId = id
            activity?.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertObject.dismissible) {
            UserPreferences.alertDisplayedId = alertObject.id
        }
        presenter.onDestroy()
    }

    override fun hideWebLink() {
        alertClickMessage.visibility = View.INVISIBLE
    }

    override fun showWebLink() {
        linkText.text = alertObject?.urlTitle
        alertClickMessage.setOnClickListener { _ ->
            if (Patterns.WEB_URL.matcher(alertObject.url).matches()) {
                val alertIntentClick = Intent(Intent.ACTION_VIEW, Uri.parse(alertObject?.url))
                startActivity(alertIntentClick)
            }
        }
    }

    override fun showFailureDialog() {
        AlertDialog.Builder(context, R.style.CUSTOM_ALERT)
                .setCancelable(false)
                .setMessage(getString(R.string.alert_failed_dialog_messege))
                .setPositiveButton(android.R.string.ok) { dialog, which ->

                }
                .show()
    }

    override fun userClickedConfirmButton(alert: Alert) {
        val acceptURL = alert.dismissButtonWebhook + "&userId=" + UserPreferences.userId
        presenter.userClickedConfirm(alert.id, acceptURL)
    }


    override fun hideRedConfirmButton() {
        acceptButton.visibility = View.GONE
    }


    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun showRedConfirmButton(alert: Alert) {
        acceptButton.visibility = View.VISIBLE
        acceptButton.text = alert.dismissButtonText
        acceptButton.setOnClickListener {
            userClickedConfirmButton(alert)
        }
    }

    companion object {

        private const val ALERT = "alert"
        fun newInstance(alert: Alert): AlertScreenFragment {
            val fragment = AlertScreenFragment()
            val args = Bundle()
            args.putParcelable(ALERT, alert)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBack(): Boolean {
        return !alertObject.dismissible
    }
}


