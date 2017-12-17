package com.moviepass.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.activities.MovieActivity;

/**
 * Created by omievee on 12/16/17.
 */

public class SynopsisFragment extends BottomSheetDialogFragment {

    public static final String TAG = " found it";
    public TextView synopsisText, synopsisTitle;
    public static final String MOVIE = "movie";
    public static final String TITLE = "title";


    public static final String SYNOPSIS = "synopsis";

    public SynopsisFragment() {

    }

    public static SynopsisFragment newInstance(String movie) {
        SynopsisFragment fragment = new SynopsisFragment();
        Bundle args = new Bundle();
        args.putString(SYNOPSIS, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fr_dialogfragment_synopsis, container);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String strtext = getArguments().getString(MOVIE);
        String title = getArguments().getString(TITLE);

        synopsisTitle = view.findViewById(R.id.SYNOPSIS_TITLE);
        synopsisText = view.findViewById(R.id.SYNOPSIS_TEXT);
        synopsisText.setText(strtext);
        synopsisTitle.setText(title);
    }
}


