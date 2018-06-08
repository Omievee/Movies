package com.mobile.loyalty

import com.mobile.network.RestClient
import com.mobile.rx.Schedulers
import io.reactivex.disposables.Disposable

class LoyaltyProgramPresenter(val loyaltyPresentationModel: LoyaltyPresentationModel, val view: LoyaltyProgramView) {

    val state = State()

    fun onResume() {
        fetchTheaterChainsIfNecessary()
    }

    private fun fetchTheaterChainsIfNecessary() {
        if (state.registeredTheaterChains != null) {
            return
        }
        view.showProgress()
        state
                .theaterChainSubscription = RestClient.getAuthenticated()
                .theaterChains()
                .doAfterTerminate { view.hideProgress() }
                .map {
                    state.allTheaterChains = it
                    state.registeredTheaterChains = it.filter { it.isUserRegistered }
                    val unregistered = state.allTheaterChains?.filter {
                        !it.isUserRegistered
                    }?.toMutableList()
                    unregistered?.add(0, TheaterChain(loyaltyPresentationModel.addLoyaltyProgram))
                    state.unregisteredTheaterChains = unregistered
                    it
                }
                .subscribe({ _ ->
                    state.error = null
                    showTheaters()
                }, { error ->
                    state.error = error
                })

    }

    private fun showTheaters() {
        val unregisteredTheaterChainSize = state.unregisteredTheaterChains?.size ?: 0
        when {
            unregisteredTheaterChainSize > 1 -> {
                state.unregisteredTheaterChains?.let {
                    view.showAddTheaters(it)
                }
            }
            else -> {
                view.hideAddTheaters()
            }
        }
        val registeredTheaterChainSize = state.registeredTheaterChains?.size ?: 0
        when {
            registeredTheaterChainSize > 0 -> {
                state.registeredTheaterChains?.let {
                    view.showRegisteredTheaters(it)
                    view.hideAddAMovieTheaterLoyaltyMessage()
                }
            }
            else -> {
                view.hideRegisteredTheaters()
                view.showAddAMovieTheaterLoyaltyMessage()
            }
        }
    }

    fun onLoyaltyProgramSelected(theaterChain: TheaterChain?) {
        theaterChain?.let {
            view.showLoyaltyScreenFields(it)
        }
        view.showSpinnerText(loyaltyPresentationModel.addLoyaltyProgram)
    }

    fun onSignInButtonClicked(theaterChain: TheaterChain, data: List<Triple<String, RequiredField, String>>) {
        state.lastTheaterChain = theaterChain
        val loyaltyDataMap = data
                .associateTo(
                        mutableMapOf(), {
                    it.first to it.third
                })
        state.lastData = loyaltyDataMap
        view.showProgress()
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
                    state.registeredTheaterChains = state.allTheaterChains?.filter {
                        it.isUserRegistered
                    }
                    val unregistered = state.allTheaterChains?.filter {
                        !it.isUserRegistered
                    }?.toMutableList()
                    unregistered?.add(0, TheaterChain(loyaltyPresentationModel.addLoyaltyProgram))
                    state.unregisteredTheaterChains = unregistered
                }
                .subscribe({ _ ->
                    view.hideLoyaltySignIn()
                    showTheaters()
                }, { _ ->
                    state.lastTheaterChain?.let { theaterChain ->
                        view.showAddLoyaltyError(theaterChain)
                    }

                })
    }

    fun retryLoyaltyProgram() {
        state.lastTheaterChain?.let {
            view.showLoyaltyScreenFields(it, state.lastData)
        }

    }
}

class State {
    var theaterChainSubscription: Disposable? = null
    var allTheaterChains: List<TheaterChain>? = null
    var registeredTheaterChains: List<TheaterChain>? = null
    var unregisteredTheaterChains: List<TheaterChain>? = null
    var error: Throwable? = null
    var lastTheaterChain: TheaterChain? = null
    var lastData: Map<String, String>? = null
}