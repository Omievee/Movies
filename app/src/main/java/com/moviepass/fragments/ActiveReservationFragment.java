package com.moviepass.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moviepass.R;

import butterknife.ButterKnife;

/**
 * Created by anubis on 7/31/17.
 */

public class ActiveReservationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_active_reservation, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }
}
