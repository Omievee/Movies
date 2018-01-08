package com.moviepass.extensions;

import android.content.Context;

/**
 * Created by o_vicarra on 1/8/18.
 */

public class Selectable extends ShowtimeButton {

    private boolean isSelected = false;


    public Selectable(Context context, String showtime, boolean isSelected) {
        super(context, showtime);
        this.isSelected = isSelected;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
