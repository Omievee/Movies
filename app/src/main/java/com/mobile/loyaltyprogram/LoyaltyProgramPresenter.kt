package com.mobile.loyaltyprogram

import android.text.Editable
import com.mobile.network.RestClient
import com.mobile.rx.Schedulers
import io.reactivex.disposables.Disposable

class LoyaltyProgramPresenter(val loyaltyPresentationModel: LoyaltyPresentationModel, val view: LoyaltyProgramView) {

    val state = State()

    fun onResume() {
        fetchTheaterChainsIfNecessary()
    }

    private fun fetchTheaterChainsIfNecessary() {
        if (state.theaterChains != null) {
            return
        }
        state
                .theaterChainSubscription = RestClient.getAuthenticated()
                .theaterChains()
                .compose(Schedulers.singleDefault())
                .subscribe({ v ->
                    state.theaterChains = v
                    state.error = null
                    state.theaterChains?.let {
                        view.showTheaters(it.toList())
                    }

                }, { error ->
                    state.error = error
                })

    }

    fun onLoyaltyProgramSelected(position: Int) {
        val theaterChain = state.theaterChains?.get(position)
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
        RestClient
                .getAuthenticated()
                .theaterChainSignIn(
                        theaterChain.chainNameKey,
                        loyaltyDataMap
                )
                .compose(Schedulers.singleDefault())
                .doAfterTerminate {
                    view.hideProgress()
                }
                .subscribe({ result ->
                    view.showLoyaltyMembership()
                    println("${result}")

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
    var theaterChains: MutableList<TheaterChain>? = null
    var error: Throwable? = null
    var lastTheaterChain: TheaterChain? = null
    var lastData: Map<String, String>? = null
}