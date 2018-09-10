package com.mobile.loyalty

import com.mobile.ApiError
import com.mobile.network.Api
import com.mobile.network.RestClient

class EditLoyaltyProgramPresenter(val view: EditLoyalProgramView, api: Api) {

    var registeredTheaterChains: List<TheaterChain>? = null
    var allTheaterChains: List<TheaterChain>? = null
    var newList: List<TheaterChain>? = null
    fun onViewCreated() {
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
                    view.updateSuccessful(false)
                }) { error ->
                    (error as? ApiError)?.let {
                        view.updateFailure(it.error.message)
                    }

                }
    }

    fun deleteLoyaltyProgram(theaterchain: TheaterChain, data: List<Triple<String, RequiredField, String>>) {
        view.showProgress()
        val mappedData =
                data
                        .associateTo(
                                mutableMapOf()) {
                            it.first to it.third
                        }

        RestClient.getAuthenticated()
                .theaterChainRemove(
                        theaterchain.chainNameKey,
                        mappedData
                )
                .doAfterTerminate { view.hideProgress() }
                .subscribe({ _ ->
                    view.updateSuccessful(true)
                }) { error ->
                    (error as? ApiError)?.let {
                        view.updateFailure(it.error.message)
                    }
                }


    }
}