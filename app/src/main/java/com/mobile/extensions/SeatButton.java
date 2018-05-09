package com.mobile.extensions;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.mobile.model.SeatInfo;
import com.moviepass.R;

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
        setBackground(null);
        mSeatInfo = seat;
        mSelected = false;

        switch (mSeatInfo.getSeatType()) {
            case SeatTypeNotASeat:
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
            case SeatTypeSofaLeft:
                if (mSeatInfo.isAvailable()) {
                    this.setImageResource(R.drawable.icon_seat_sofa_left_available);
                } else {
                    this.setImageResource(R.drawable.icon_seat_sofa_left_unavailable);
                }
                break;
            case SeatTypeSofaRight:
                if(mSeatInfo.isAvailable()) {
                    this.setImageResource(R.drawable.icon_seat_sofa_right_available);
                } else {
                    this.setImageResource(R.drawable.icon_seat_sofa_right_unavailable);
                }
                break;
            case SeatTypeSofaMiddle:
                if(mSeatInfo.isAvailable()) {
                    this.setImageResource(R.drawable.icon_seat_sofa_middle_available);
                } else {
                    this.setImageResource(R.drawable.icon_seat_sofa_middle_unavailable);
                }
                break;
            case SeatTypeWheelchair:
                if (mSeatInfo.isAvailable())
                    this.setImageResource(R.drawable.icon_seat_wheelchair_available);
                else
                    this.setImageResource(R.drawable.icon_seat_wheelchair_unavailable);
                break;
            case SeatTypeCompanion:
                if (seat.isAvailable()) {
                    this.setImageResource(R.drawable.icon_seat_companion_available);
                } else {
                    this.setImageResource(R.drawable.icon_seat_companion_unavailable);
                }
                break;
        }

        this.setEnabled(mSeatInfo.isAvailable() && (mSeatInfo.getSeatType() != SeatInfo.SeatType.SeatTypeCompanion));
    }

    public void setSeatSelected(Boolean selected) {
        if (selected) {
            switch (mSeatInfo.getSeatType()) {
                case SeatTypeCanReserve:
                case SeatTypeCanReserveLeft:
                case SeatTypeCanReserveRight:
                    this.setImageResource(R.drawable.icon_seat_selected);
                    break;
                case SeatTypeWheelchair:
                    this.setImageResource(R.drawable.icon_seat_wheelchair_selected);
                    break;
                case SeatTypeCompanion:
                    this.setImageResource(R.drawable.icon_seat_companion_selected);
                    break;
                case SeatTypeSofaMiddle:
                    this.setImageResource(R.drawable.icon_seat_sofa_middle_selected);
                    break;
                case SeatTypeSofaRight:
                    this.setImageResource(R.drawable.icon_seat_sofa_right_selected);
                    break;
                case SeatTypeSofaLeft:
                    this.setImageResource(R.drawable.icon_seat_sofa_left_selected);
                    break;
            }
        } else {
            initSeat(mSeatInfo);
        }
    }

    public String getSeatName() {
        return mSeatInfo.getSeatName();
    }

    public SeatInfo getSeatInfo() {
        return mSeatInfo;
    }
}