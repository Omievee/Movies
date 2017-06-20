package com.moviepass;

import android.widget.ImageView;

import com.moviepass.model.Movie;

/**
 * Created by ryan on 4/27/17.
 */

public interface MoviePosterClickListener {

    void onMoviePosterClick(int pos, Movie movie, ImageView sharedImageView);
}
