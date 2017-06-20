package com.moviepass;

import com.moviepass.model.Screening;
import com.moviepass.model.Theater;

/**
 * Created by ryan on 4/27/17.
 */

public interface MovieTheaterClickListener {

    void onTheaterClick(int pos, Screening screening);
}
