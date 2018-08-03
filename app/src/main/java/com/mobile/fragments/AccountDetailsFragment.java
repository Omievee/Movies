package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobile.UserPreferences;
import com.mobile.responses.PeakPassInfo;
import com.mobile.seats.MPBottomSheetFragment;
import com.mobile.seats.SheetData;
import com.moviepass.R;

import java.text.SimpleDateFormat;

/*
 * Created by anubis on 9/2/17.
 */

public class AccountDetailsFragment extends MPFragment {

    Context context;
    View rootView, progress, accountInformation, changePassword;
    RelativeLayout shippingClick, billingClick;
    View peakPassContainer, newPeakPasses;
    TextView nextPeakPass, peakPasses;
    Activity myActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account_details, container, false);


        accountInformation = rootView.findViewById(R.id.UP);
        shippingClick = rootView.findViewById(R.id.MIDDLE);
        billingClick = rootView.findViewById(R.id.END);
        changePassword = rootView.findViewById(R.id.changePassword);
        nextPeakPass = rootView.findViewById(R.id.nextPeakPass);
        peakPassContainer = rootView.findViewById(R.id.peakContainer);
        newPeakPasses = rootView.findViewById(R.id.newPeakPassHeader);
        peakPasses = rootView.findViewById(R.id.peakPasses);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setPeakPass();
    }

    private void setPeakPass() {
        boolean hasNewPeakPass = UserPreferences.INSTANCE.getHasNewPeakPass();
        PeakPassInfo pinfo = UserPreferences.INSTANCE.getRestrictions().getPeakPassInfo();
        peakPassContainer.setVisibility(pinfo.getEnabled() ? View.VISIBLE : View.GONE);
        if (!pinfo.getEnabled()) {
            return;
        }
        peakPasses.setText(getResources().getQuantityString(R.plurals.passes, pinfo.getPeakPasses().size(), pinfo.getPeakPasses().size()));
        if (TextUtils.isEmpty(pinfo.getNextRefillDate())) {
            nextPeakPass.setVisibility(View.GONE);
        } else {
            nextPeakPass.setVisibility(pinfo.getPeakPasses().size() == 0 ? View.VISIBLE : View.GONE);
            nextPeakPass.setText(getResources().getString(R.string.next_pass_applied, pinfo.getNextRefillDate()));
        }
        peakPassContainer.setOnClickListener(v -> {
            MPBottomSheetFragment.Companion.newInstance(
                    new SheetData(
                            getResources().getString(R.string.peak_pass),
                            getResources().getString(R.string.peak_pass_apply_bottom),
                            null,
                            pinfo.getCurrentPeakPass() == null || pinfo.getCurrentPeakPass().getExpires() == null
                                    ? null
                                    : getResources().getString(R.string.peak_pass_expires, pinfo.getCurrentPeakPass().expiresAsString()),
                            Gravity.CENTER
                    )
            ).show(getChildFragmentManager(), "");
        });
        newPeakPasses.setVisibility(hasNewPeakPass ? View.VISIBLE : View.GONE);
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



