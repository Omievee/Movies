package com.mobile.reservation

import android.widget.Toast
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.network.Api
import com.mobile.requests.ChangedMindRequest
import com.mobile.utils.onBackExtension
import io.reactivex.disposables.Disposable

class ReservationActivityPresenter(val view: ReservationActivity, val api: Api) {
    private var cancelReservationDisposable: Disposable? = null
    private var userInfoDisposable: Disposable? = null

    private var zipCode:String? = null

    fun cancelCurrentReservation(reservation: Int) {
        val request = ChangedMindRequest(reservation)
        cancelReservationDisposable?.dispose()
        cancelReservationDisposable = api
                .changedMind(request)
                .subscribe({
                    Toast.makeText(view, it.message, Toast.LENGTH_SHORT).show()
                    view.hideProgress()
                    view.finish()
                }
                        , { error ->
                    if (error is ApiError) {
                        Toast.makeText(view, error.error.message, Toast.LENGTH_SHORT).show()
                    }
                    view.hideProgress()
                })
    }

    fun getUserZipCode() {
        userInfoDisposable?.dispose()
        userInfoDisposable = api
                .getUserDataRx(UserPreferences.userId)
                .subscribe({
                    UserPreferences.zipCode = it.billingZipCode
                },{

                })
    }

    private fun fetchZipCode() {
        if (zipCode != null) {
            return
        }
        userInfoDisposable?.dispose()
        userInfoDisposable = api.getUserDataRx(
                UserPreferences.userId
        ).subscribe({ data ->
            val zip = data.billingZipCode?: return@subscribe
            zipCode = zip
            UserPreferences.zipCode = zip
            view.showZipCode(zip)
        }, {

        })
    }

    fun onResume() {
        fetchZipCode()
    }

    fun onPause() {
        userInfoDisposable?.dispose()
    }
}
