package com.mobile.history

import com.mobile.history.StarsRating.Rating
import com.mobile.history.model.ReservationHistory

interface HistoryDetailViewListener {
    fun ratingFromRatingScreenSubmited()
    fun fillUpStars(rating: Rating?)
    fun setClickListeners()
    fun bindFromHistoryTap(res: ReservationHistory?, historyDetailsListener: HistoryDetailDismissListener?=null, presenter: HistoryDetailPresenter)
    fun bindRateLastMovie(res: ReservationHistory?, historyDetailsListener: HistoryDetailDismissListener?=null, presenter: HistoryDetailPresenter)
    fun close()
    fun emptyStars()
}