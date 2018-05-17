package com.mobile.loyalty

interface LoyaltyProgramView {
    fun showAddTheaters(theaterChains: List<TheaterChain>)
    fun showSpinnerText(text: String?)
    fun showLoyaltyScreenFields(theaterChain: TheaterChain, triple: Map<String, String>? = null)
    fun showProgress()
    fun hideProgress()
    fun showAddLoyaltyError(theaterChain: TheaterChain)
    fun hideAddTheaters()
    fun showRegisteredTheaters(theaters: List<TheaterChain>)
    fun hideRegisteredTheaters()
    fun hideLoyaltySignIn()
    fun hideAddAMovieTheaterLoyaltyMessage()
    fun showAddAMovieTheaterLoyaltyMessage()
}