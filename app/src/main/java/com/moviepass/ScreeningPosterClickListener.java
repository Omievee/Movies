package com.moviepass;

import android.widget.ImageView;

import com.moviepass.model.Screening;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anubis on 6/10/17.
 */

public interface ScreeningPosterClickListener {

    void onScreeningPosterClick(int pos, Screening screening, List<String> startTimes, ImageView shareImageView);

}
