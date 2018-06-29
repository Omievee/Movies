package com.mobile.extensions;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.mobile.model.SeatInfo;
import com.moviepass.R;

public class SeatButton extends AppCompatImageButton {

    private SeatInfo mSeatInfo;

    public SeatButton(Context context) {
        super(context);
    }

    public SeatButton(Context context, SeatInfo seat) {
        super(context);
        initSeat(seat);
    }

    public SeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeatButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initSeat(SeatInfo seat) {
        setBackground(null);
        mSeatInfo = seat;

        setImageDrawable(SeatHelper.Companion.getDrawable(seat, getResources()));
        this.setEnabled(mSeatInfo.isAvailable());
    }

    public void setSeatSelected(Boolean selected) {
        setSelected(selected);
    }

    public String getSeatName() {
        return mSeatInfo.getSeatName();
    }

    public SeatInfo getSeatInfo() {
        return mSeatInfo;
    }
}