package com.mobile.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.moviepass.R;


public class NoInternetFragment extends DialogFragment {

    Button tryAgain;
    View rootview;


    public NoInternetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.fr_no_internet, container, false);

        tryAgain = rootview.findViewById(R.id.Tryagain);
        return rootview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    getActivity().recreate();
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Unable to detect an active network connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public boolean isOnline() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = connectivityManager.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
}
