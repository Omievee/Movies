package com.moviepass.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.activities.EticketConfirmation;
import com.moviepass.activities.SelectSeatActivity;
import com.moviepass.model.Screening;
import com.moviepass.model.SelectedSeat;

import org.parceler.Parcels;

import java.util.List;

/**
 * Created by anubis on 5/31/17.
 */

public class ETicketFragment extends DialogFragment {
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String SEAT = "seat";
    EticketConfirmation confirm = new EticketConfirmation();
    public static final String PATTERN = "630425";
    View rootView;
    PatternLockView lockView;
    PatternLockViewListener getmPatternLockViewListener;
    String getShowtime;
    SelectedSeat getSeat;
    Screening getTitle;


    public ETicketFragment() {
    }

    public static ETicketFragment newInstance() {
        return new ETicketFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fr_eticketconfirm_noticedialog, container);

        lockView = rootView.findViewById(R.id.LOCKVIEW);


        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        lockView.addPatternLockListener(mPatternLockViewListener);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            getTitle = Parcels.unwrap(bundle.getParcelable(SCREENING));
            getShowtime = bundle.getString(SHOWTIME);
            getSeat = Parcels.unwrap(bundle.getParcelable(SEAT));
            Log.d(Constants.TAG, "onViewCreated:  " + getSeat.getSelectedSeatRow());
            Log.d(Constants.TAG, "onViewCreated: " + getTitle.getTitle());
        }
    }

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
        }


        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {

        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            if (PatternLockUtils.patternToString(lockView, pattern).equals("6304258")) {

                confirm.reserve(getTitle, getShowtime, getSeat);

                dismiss();

            } else {
                Toast.makeText(getActivity(), "Incorrect Pattern", Toast.LENGTH_SHORT).show();
                lockView.clearPattern();

            }

        }

        @Override
        public void onCleared() {

        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dismiss();

    }
}
