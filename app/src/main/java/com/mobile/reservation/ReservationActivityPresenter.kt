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

    fun onPause() {
        cancelReservationDisposable?.dispose()
    }
}
