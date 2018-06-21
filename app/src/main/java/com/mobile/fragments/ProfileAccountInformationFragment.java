package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.moviepass.R;

/*
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends MPFragment {

    Context context;
    View rootView, progress, accountInformation, changePassword;
    RelativeLayout shippingClick, billingClick;
    Activity myActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_account_details, container, false);


        accountInformation = rootView.findViewById(R.id.UP);
        shippingClick = rootView.findViewById(R.id.MIDDLE);
        billingClick = rootView.findViewById(R.id.END);
        changePassword = rootView.findViewById(R.id.changePassword);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        shippingClick.setOnClickListener(v -> showFragment(new ProfileAccountShippingInformation()));

        accountInformation.setOnClickListener(v -> showFragment(new ProfileAccountInformation()));

        billingClick.setOnClickListener(v -> showFragment(new ProfileAccountPlanAndBilling()));

        changePassword.setOnClickListener(v -> showFragment(new ProfileAccountChangePassword()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
    }

}



