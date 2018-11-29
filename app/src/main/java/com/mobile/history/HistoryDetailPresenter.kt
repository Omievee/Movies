package com.mobile.history

import android.os.Handler
import com.mobile.history.StarsRating.Rating
import com.mobile.history.model.ReservationHistory
import io.reactivex.disposables.Disposable

class HistoryDetailPresenter(val historyManager: HistoryManager) {

    var historySub: Disposable? = null
    var isHistoryScreen: Boolean = true
    var reservations: List<ReservationHistory>? = null
    var view: HistoryDetailViewListener? = null
    var dismissListener: HistoryDetailDismissListener? = null

    fun onCreate(fromRateScreen: Boolean, position: Int?, view: HistoryDetailViewListener?, dismissListener: HistoryDetailDismissListener?) {
        val reservationPosition = position ?: return
        this.view = view
        isHistoryScreen = fromRateScreen
        this.dismissListener = dismissListener
        loadData(reservationPosition)
    }

    private fun bind(reservation: ReservationHistory?) {
        val res = reservation ?: return
        when (isHistoryScreen) {
            true -> view?.bindFromHistoryTap(res, dismissListener, this)
            false -> view?.bindRateLastMovie(res, dismissListener, this)
        }
        checkIfUserHasRatedFilm(res)
        view?.setClickListeners()
    }

    fun userClickedRating(history: ReservationHistory?, rating: Rating) {
        val ratedHistory = history ?: return
        if(ratedHistory != rating){
            historySub?.dispose()
            changeRating(rating)
            historySub = historyManager.submitRatingV2(ratedHistory, rating)
                    .subscribe({
                        onHistorySaved()
                    }, {
                    })
        }
    }

    private fun loadData(position: Int) {
        when (reservations) {
            null -> {
                historySub?.dispose()
                historySub = historyManager.getHistory().subscribe({ res ->
                    reservations = res
                    bind(reservations?.get(position))
                }, { error ->
                })
            }
            else -> {
                bind(reservations?.get(position))
            }
        }
    }

    private fun checkIfUserHasRatedFilm(historyItem: ReservationHistory) {
        val rating = historyItem.rating
        when(rating == Rating.UNKNOWN){
            true -> view?.emptyStars()
            else -> view?.fillUpStars(rating)
        }
    }

    private fun changeRating(rating: Rating) {
        when(rating == Rating.UNKNOWN){
            true -> view?.emptyStars()
            else -> view?.fillUpStars(rating)
        }
    }
    
    private fun  onHistorySaved() {
        if (!isHistoryScreen) {
            view?.ratingFromRatingScreenSubmited()
            Handler().postDelayed({ view?.close() }, 1500)
        }
    }

    fun onDestroy() {
        historySub?.dispose()
    }
}