package com.mobile.history;

import com.mobile.history.model.ReservationHistory;

/**
 * Created by o_vicarra on 3/27/18.
 */

public interface HistoryPosterClickListener {

    void onPosterClicked(int pos, ReservationHistory historyposter, boolean isRatingScreen);
}