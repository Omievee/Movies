package com.mobile.home

import android.os.Build
import com.mobile.ApiError
import com.mobile.UserPreferences
import com.mobile.network.Api
import com.mobile.session.SessionManager
import io.reactivex.disposables.Disposable
import com.mobile.responses.AndroidIDVerificationResponse



class HomeActivityPresenter(val view:HomeActivityView, val api: Api, val sessionManager: SessionManager) {

    var androidIdDisposable:Disposable? = null
    var restrictionsDisposable:Disposable? = null
    var deviceId:String? = null

    fun onDeviceId(deviceId:String?) {
        this.deviceId = deviceId
    }

    fun onResume() {
        checkOneDevice()
    }

    private fun checkOneDevice() {
        androidIdDisposable?.dispose()

        val userId = sessionManager.getUser()?.id?.toString() ?: return
        val deviceId = this.deviceId ?: return
        val device = Build.DEVICE?:return
        val deviceType = "ANDROID"

        val request = AndroidIDVerificationResponse(device, deviceId, deviceType, true)
        androidIdDisposable = api
                .verifyAndroidIDRx(
                        userId,
                        request
                ).subscribe({result->
                    UserPreferences.setOneDeviceId(result.oneDeviceId);
                    checkRestrictions()
                },{error->
                    if(error is ApiError) {
                        if(error.httpErrorCode/100==4) {
                            sessionManager.logout()
                            view.logout()
                        }
                    }
                })
    }

    private fun checkRestrictions() {
        restrictionsDisposable?.dispose()
        val userId = sessionManager.getUser()?.id?.toString() ?: return
        //restrictionsDisposable = api.getInterstitialAlert()
    }

    fun onDestroy() {
        androidIdDisposable?.dispose()
        restrictionsDisposable?.dispose()
    }
}