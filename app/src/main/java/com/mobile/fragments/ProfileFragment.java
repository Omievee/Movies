package com.mobile.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.moviepass.R;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends Fragment {

    ProfileAccountInformationFragment profileAccountInformationFragment = new ProfileAccountInformationFragment();
    PastReservations pastReservations = new PastReservations();
    PendingReservationFragment pendingReservationFragment = new PendingReservationFragment();
    View root;
    RelativeLayout details, history, currentRes, refferals;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fr_profile, container, false);

        details = root.findViewById(R.id.Details);
        history = root.findViewById(R.id.History);
        currentRes = root.findViewById(R.id.Current);
        refferals = root.findViewById(R.id.Refs);

        fadeIn(root);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        details.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, profileAccountInformationFragment);
            transaction.addToBackStack(null);
            transaction.commit();


        });

        history.setOnClickListener(view2 -> {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, pastReservations);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        currentRes.setOnClickListener(view1 -> {
                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                    transaction.replace(R.id.profile_container, pendingReservationFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                }
        );
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }
}