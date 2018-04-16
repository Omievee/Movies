package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.helpshift.support.Log;
import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.moviepass.R;

/*
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends android.app.Fragment {

    Context context;
    View rootView, progress, accountInformation, changePassword;
    RelativeLayout shippingClick, billingClick;
    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };
    private ProfileActivityInterface mListener;
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


        shippingClick.setOnClickListener(v -> {
            mListener.openProfileAccountShippingInformation();
        });


        accountInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openProfileAccountInformation();
            }
        });


        billingClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openProfileAccountPlanAndInfo();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openChangePassword();
            }
        });



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Log.d(Constants.TAG, "onAttach cntx: ");

        if (context instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.myActivity = activity;
        Log.d(Constants.TAG, "onAttach ACT: ");

        if (activity instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
        mListener = null;
    }
}



