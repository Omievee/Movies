package com.mobile.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by anubis on 5/31/17.
 */

public class NotificationFragment extends Fragment {

    private OnFragmentInteractionListener listener;


    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NotificationFragment.OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {
    }
}
