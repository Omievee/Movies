package com.moviepass.extensions;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.moviepass.R;
import com.moviepass.model.SeatInfo;

/**
 * Created by sebacancinos on 3/9/15.
 */

public class SeatButton extends AppCompatImageButton {

    private SeatInfo mSeatInfo;
    private boolean mSelected;

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

    private void initSeat(SeatInfo seat) {
        mSeatInfo = seat;
        mSelected = false;

        this.setBackgroundResource(R.color.white);

        switch (mSeatInfo.getSeatType()) {
            case SeatTypeCompanion:
                break;

            case SeatTypeNotASeat:
                this.setImageResource(R.drawable.icon_seat_not_a_seat);
                break;

            case SeatTypeWheelchair:
                if (mSeatInfo.isAvailable())
                    this.setImageResource(R.drawable.icon_seat_wheelchair_available);
                else
                    this.setImageResource(R.drawable.icon_seat_wheelchair_unavailable);
                break;

            case SeatTypeCanReserve:
            case SeatTypeCanReserveLeft:
            case SeatTypeCanReserveRight:
            case SeatTypeUnknown:
                if (mSeatInfo.isAvailable())
                    this.setImageResource(R.drawable.icon_seat_available);
                else
                    this.setImageResource(R.drawable.icon_seat_unavailable);
                break;
        }

        this.setEnabled(mSeatInfo.isAvailable() && (mSeatInfo.getSeatType() != SeatInfo.SeatType.SeatTypeCompanion));
    }

    public void setSeatSelected(Boolean selected) {
        mSelected = selected;

        if (mSelected) {
            switch (mSeatInfo.getSeatType()) {
                case SeatTypeCompanion:
                case SeatTypeCanReserve:
                case SeatTypeCanReserveLeft:
                case SeatTypeCanReserveRight:
                    this.setImageResource(R.drawable.icon_seat_selected);
                    break;

                case SeatTypeWheelchair:
                    this.setImageResource(R.drawable.icon_seat_wheelchair_selected);
                    break;
            }
        } else {
            switch (mSeatInfo.getSeatType()) {
                case SeatTypeCanReserve:
                case SeatTypeCanReserveLeft:
                case SeatTypeCanReserveRight:
                case SeatTypeUnknown:
                    if (mSeatInfo.isAvailable())
                        this.setImageResource(R.drawable.icon_seat_available);
                    else
                        this.setImageResource(R.drawable.icon_seat_unavailable);
                    break;
                case SeatTypeWheelchair:
                    if (mSeatInfo.isAvailable())
                        this.setImageResource(R.drawable.icon_seat_wheelchair_available);
                    else
                        this.setImageResource(R.drawable.icon_seat_wheelchair_unavailable);
                    break;


            }
        }
    }

    public String getSeatName() {
        return mSeatInfo.getSeatName();
    }

    public SeatInfo getSeatInfo() {
        return mSeatInfo;
    }
}