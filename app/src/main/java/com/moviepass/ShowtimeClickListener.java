package com.moviepass;

import com.moviepass.model.Screening;

/**
 * Created by anubis on 6/13/17.
 */

public interface ShowtimeClickListener {

    void onShowtimeClick(int pos, Screening screening, String showtime);

}
