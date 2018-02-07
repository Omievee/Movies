package com.mobile.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moviepass.R;

/**
 * Created by o_vicarra on 2/7/18.
 */

public class MoviepassCardAutoActivated extends BottomSheetDialogFragment {

    View root;

    public MoviepassCardAutoActivated() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fr_mpcard_autoactivated, container, false);


        return root;
    }


}
