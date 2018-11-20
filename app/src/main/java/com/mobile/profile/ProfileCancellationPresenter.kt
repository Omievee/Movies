package com.mobile.profile

import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.extensions.DropDownFields
import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.requests.CancellationRequest
import com.moviepass.R
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*

class ProfileCancellationPresenter(val api: Api, val billingAPi: BillingApi, val view: ProfileCancellationView) {

    var userInfo: Disposable? = null
    var profileCancellationDisposable: Disposable? = null
    var billingDate: Date? = null

    private fun loadUserInfo() {
        userInfo?.dispose()
        val userId = UserPreferences.userId
        userInfo = api.getUserDataRx(userId).subscribe({
            billingDate = it.nextBillingDate
        }, {

        })
    }

    fun onCreate() {
        loadUserInfo()
        view.bindView()
    }

    fun onResume() {
        if (billingDate == null)
            loadUserInfo()
    }

    fun onSubmitCancellation(reason: String) {
        if (reason == DropDownFields.UNKNOWN.type)
            view.showErrorDialog(R.string.cancellation_drop_drown_error)
        else {
            view.showCancellationConfirmationDialog(reason, "", billingDate)
        }
    }

    fun cancelFlow(reason: String) {
        var cancelSubscriptionReason: Long = CancellationReason.getReasonNumber(reason)
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val requestDate = df.format(c.time)

        view.showProgress()

        val request = CancellationRequest(requestDate, cancelSubscriptionReason, "")

        profileCancellationDisposable?.dispose()

        profileCancellationDisposable =
                billingAPi
                        .requestCancellation(request)
                        .doAfterTerminate { view.hideProgress() }
                        .subscribe({
                            view.successfullCancellation(it?.nextBillingDate)
                        })
                        { error ->
                            when (error) {
                                is ApiError -> {
                                    view.unccessfullCancellation(error.error.message)
                                }
                            }
                        }
    }

    fun onDestroy() {
        userInfo?.dispose()
        profileCancellationDisposable?.dispose()
    }

}