package com.mobile.Interfaces;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.adapters.HistoryAdapter;
import com.mobile.model.Movie;

/**
 * Created by o_vicarra on 3/27/18.
 */

public interface historyPosterClickListener {

    void onPosterClicked(int pos, Movie historyposter, SimpleDraweeView sharedView);
}