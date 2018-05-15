package com.mobile.widgets

interface LoyaltyProgramView {
    fun showTheaters(theaterChains: List<TheaterChain>)
    fun showSpinnerText(text: String?)
    fun showLoyaltyScreenFields(theaterChain: TheaterChain, triple: Map<String, String>? = null)
    fun showProgress()
    fun hideProgress()
    fun showLoyaltyMembership()
    fun showAddLoyaltyError(theaterChain: TheaterChain)
}