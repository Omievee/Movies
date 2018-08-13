package com.mobile.profile

import android.util.Log
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.extensions.DropDownFields
import com.mobile.network.Api
import com.mobile.requests.CancellationRequest
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*

class ProfileCancellationPresenter(var api: Api, var view: ProfileCancellationView){

    var userInfo : Disposable ? = null
    var profileCancellationDisposable: Disposable ? = null
    var billingDate: String ? = null

    private fun loadUserInfo() {
        userInfo?.dispose()
        val userId = UserPreferences.userId
        userInfo = api.getUserDataRx(userId).subscribe({
            billingDate = it.nextBillingDate
        },{

        })
    }

    fun onCreate(){
        loadUserInfo()
        view.bindView()
    }

    fun onResume(){
        if(billingDate == null)
            loadUserInfo()
    }

    fun onSubmitCancellation(reason: String, comment: String){
        if(reason == DropDownFields.UNKNOWN.type)
            view.showErrorMessage()
        else {
            view.showCancellationConfirmationDialog(reason, comment, billingDate)
        }
    }

    fun cancelFlow(reason: String, comment: String) {
        var cancelSubscriptionReason: Long = CancellationReason.getReasonNumber(reason)
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val requestDate = df.format(c.time)

        view.showProgress()

        val request = CancellationRequest(requestDate, cancelSubscriptionReason, comment)

        profileCancellationDisposable?.dispose()

        profileCancellationDisposable =
                api
                        .requestCancellation(request)
                        .doAfterTerminate { view.hideProgress() }
                        .subscribe({
                            view.successfullCancellation(it.nextBillingDate)
                        })
                        { error ->
                            when(error) {
                                is ApiError -> view.unccessfullCancellation(error.error.message)
                            }
                        }
    }

    fun onDestroy(){
        userInfo?.dispose()
        profileCancellationDisposable?.dispose()
    }

}