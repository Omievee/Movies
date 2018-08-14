package com.mobile.alertscreen

import com.mobile.model.Alert
import okhttp3.*
import java.io.IOException


class AlertScreenPresenter(val view: AlertScreenView) {


    fun onCreateView(alert: Alert) {

        setAlert(alert)
        determineIfDismissible(alert)
        determineShowWebLink(alert)
        determineIfShowAcceptButton(alert)

    }

    private fun determineIfShowAcceptButton(alert: Alert) {
        when (alert.dismissButton) {
            true -> view.showRedConfirmButton(alert)
            false -> view.hideRedConfirmButton()
        }
    }

    private fun determineShowWebLink(alert: Alert) {
        if (alert.urlTitle.isNullOrEmpty() || alert.url.isNullOrEmpty()) {
            view.hideWebLink()
        } else {
            view.showWebLink()
        }
    }

    private fun setAlert(alert: Alert) {
        view.setAlert(alert)
    }

    private fun determineIfDismissible(alert: Alert) {
        when (alert.dismissible) {
            true -> view.dismissAlertScreen(alert.id)
            false -> view.hideDismissIcon()
        }

    }

    fun userClickedConfirm(id: String?, url: String?) {
        id ?: return
        url ?: return

        if (run(url)) {
            view.dismissAlertScreen(id)
        } else {
            view.showFailureDialog()
        }
    }


    @Throws(IOException::class)
    fun run(url: String): Boolean {

        val client = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .build()

        var passedResponse = false
        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                passedResponse = false
            }

            override fun onResponse(call: Call?, response: Response?) {
                response ?: return
                passedResponse = response.isSuccessful
            }

            @Throws(IOException::class)
            fun onResponse(response: Response) {
            }
        });
        return passedResponse
    }

}