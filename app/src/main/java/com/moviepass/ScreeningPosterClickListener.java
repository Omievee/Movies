package com.moviepass;

import android.widget.ImageView;

import com.moviepass.model.Screening;

/**
 * Created by anubis on 6/10/17.
 */

public interface ScreeningPosterClickListener {

    void onScreeningPosterClick(int pos, Screening screening, ImageView shareImageView);

}
