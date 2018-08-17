package com.mobile.alertscreen

import com.mobile.model.Alert
import com.mobile.rx.Schedulers
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.disposables.Disposable
import okhttp3.*
import java.io.IOException


class AlertScreenPresenter(val view: AlertScreenView) {

    var dismissSub:Disposable?=null

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

    fun onDestroy(){
        dismissSub?.dispose()
    }

    fun userClickedConfirm(id: String?, url: String?) {
        id ?: return
        url ?: return

        dismissSub = Single.create(SingleOnSubscribe<Boolean> {
            try {
                val result = run(url)
                if (!it.isDisposed) {
                    it.onSuccess(result)
                }
            } catch (e: Exception) {
                if (!it.isDisposed) {
                    it.onSuccess(false)
                }
            }
        }).compose(Schedulers.singleDefault()).subscribe { t1, _ ->
            if (t1 == true) {
                view.dismissAlertScreen(id)
            } else {
                view.showFailureDialog()
            }
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
                passedResponse = response.code() == 201
            }

            @Throws(IOException::class)
            fun onResponse(response: Response) {
            }
        });
        return passedResponse
    }

}