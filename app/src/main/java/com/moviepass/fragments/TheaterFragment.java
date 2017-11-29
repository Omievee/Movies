package com.moviepass.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.model.Theater;

import org.parceler.Parcels;

import butterknife.ButterKnife;


/**
 * Created by anubis on 6/8/17.
 */

public class TheaterFragment extends Fragment {

    public static final String THEATER = "theater";
    Theater mTheater;
    TextView theaterSelectedName;


    public static TheaterFragment newInstance() {
        return new TheaterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_theater, container, false);
        ButterKnife.bind(this, rootView);

        Bundle extras = getArguments();
        mTheater = Parcels.unwrap(getActivity().getIntent().getParcelableExtra(THEATER));


//        theaterSelectedName.setText(mTheater.getTitle());

//        mTheaterName = rootView.findViewById(R.id.theater_name);
//        mTheaterAddress = rootView.findViewById(R.id.theater_address);
//
//        mTheaterName.setText(mTheater.getName());
//        mTheaterAddress.setText(mTheater.getAddress());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();

    }


    @Override
    public void onStop() {
        super.onStop();
    }


}