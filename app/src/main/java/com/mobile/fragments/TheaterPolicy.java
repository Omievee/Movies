package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.moviepass.R;

/**
 * Created by o_vicarra on 1/24/18.
 */

public class TheaterPolicy extends BottomSheetDialogFragment {

    public static final String POLICY = "policy";
    TextView theaterName;
    ImageButton close;
    Context myContext;
    Activity myActivity;
    public TheaterPolicy() {
    }


    public static TheaterPolicy newInstance(String movie) {
        TheaterPolicy fragment = new TheaterPolicy();
        Bundle args = new Bundle();
        args.putString(POLICY, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_theaterpolicy, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String theaterText = getArguments().getString(POLICY);

        close = view.findViewById(R.id.close);
        theaterName = view.findViewById(R.id.TheaterName);
        theaterName.setText(theaterText);


        close.setOnClickListener(v -> {

        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myContext = null;
    }
}
