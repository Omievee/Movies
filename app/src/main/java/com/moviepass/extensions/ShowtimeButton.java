package com.moviepass.extensions;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;

/**
 * Created by o_vicarra on 1/8/18.
 */

public class ShowtimeButton extends AppCompatButton {
    private String showtime;


    public ShowtimeButton(Context context, String showtime) {
        super(context);
        this.showtime = showtime;
    }

    public String getShowtime() {
        return showtime;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;


        ShowtimeButton showtimeCompare = (ShowtimeButton) obj;
        if (showtimeCompare.getShowtime().equals(this.getShowtime()))
            return true;

        return false;
    }
}



