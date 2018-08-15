package com.mobile.loyalty

import com.mobile.ApiError
import com.mobile.network.Api
import com.mobile.network.RestClient
import com.moviepass.R
import io.reactivex.disposables.Disposable

class EditLoyaltyProgramPresenter(val view: EditLoyalProgramView, api: Api) {

    var theaterChainSubscription: Disposable? = null
    var registeredTheaterChains: List<TheaterChain>? = null
    var allTheaterChains: List<TheaterChain>? = null

    fun onViewCreated(theaterChain: TheaterChain) {
        retrieveLoyaltyData()
    }

    private fun retrieveLoyaltyData() {
        view.setLoyaltyData()

    }

    fun userUpdatedLoyaltyCard(theaterChain: TheaterChain, data: List<Triple<String, RequiredField, String>>) {
        view.showProgress()
        val loyaltyDataMap = data
                .associateTo(
                        mutableMapOf()) {
                    it.first to it.third
                }
        RestClient.getAuthenticated()
                .theaterChainSignIn(
                        theaterChain.chainNameKey,
                        //TODO: TEST UPDATE LOYALTY
                        loyaltyDataMap
                )
                .doAfterTerminate {
                    view.hideProgress()
                }
                .map {
                    theaterChain.isUserRegistered = true
                    registeredTheaterChains = allTheaterChains?.filter {
                        it.isUserRegistered
                    }
                }
                .subscribe({ _ ->
                    view.updateSuccessful()
                }) { error ->
                    (error as? ApiError)?.let {
                        view.updateFailure(it.error.message)
                    } ?: view.updateFailure(R.string.generic_error)

                }
    }

    fun deleteLoyaltyProgram() {
//        view.showProgress()
        //TODO: DELETE LOYALTY
    }
}