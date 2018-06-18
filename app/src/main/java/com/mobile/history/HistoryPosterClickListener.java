package com.mobile.history;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.history.model.ReservationHistory;

/**
 * Created by o_vicarra on 3/27/18.
 */

public interface HistoryPosterClickListener {

    void onPosterClicked(int pos, ReservationHistory historyposter, SimpleDraweeView sharedView);
}