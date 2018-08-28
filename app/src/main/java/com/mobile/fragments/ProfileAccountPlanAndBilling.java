package com.mobile.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.UserPreferences;
import com.mobile.billing.MissingBillingFragment;
import com.mobile.network.RestClient;
import com.mobile.profile.ProfileCancellationFragment;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileAccountPlanAndBilling extends MPFragment {


    private View rootView;
    private TextView billingDate, plan, planPrice, planCancel, billingCard, billingChange;
    UserInfoResponse userInfoResponse;
    Context myContext;
    View progress;
    private Activity myActivity;

    public ProfileAccountPlanAndBilling() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile_account_plan_and_billing, container, false);


        billingDate = rootView.findViewById(R.id.BillingDate);
        plan = rootView.findViewById(R.id.Plan);
        planPrice = rootView.findViewById(R.id.Plan_Data);
        planCancel = rootView.findViewById(R.id.PLan_cancel);
        billingCard = rootView.findViewById(R.id.USER_BILLING);
        billingChange = rootView.findViewById(R.id.Billing_Change);
        progress = rootView.findViewById(R.id.progress);


        loadUserInfo();

        planCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new ProfileCancellationFragment());
            }
        });

        billingChange.setOnClickListener(v -> {

            showFragment(new MissingBillingFragment());

        });


        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
        myActivity = getActivity();
    }


    private void loadUserInfo() {
        int userId = UserPreferences.INSTANCE.getUserId();
        progress.setVisibility(View.VISIBLE);
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                progress.setVisibility(View.GONE);
                if (userInfoResponse != null) {
                    UserPreferences.INSTANCE.saveBilling(userInfoResponse);

                    billingCard.setText(userInfoResponse.getBillingCard());
                    plan.setText(userInfoResponse.getPlan());
                    progress.setVisibility(View.GONE);

                    if (userInfoResponse.getBillingCard() == null || userInfoResponse.getBillingCard().isEmpty()) {
                        billingChange.setText(getString(R.string.add_payment_method));
                    }

                    if (userInfoResponse.getNextBillingDate() == null) {
                        billingDate.setText("Unknown");
                    } else {
                        SimpleDateFormat SDFormat = new SimpleDateFormat("MMMM dd, yyyy");
                        billingDate.setText(SDFormat.format(userInfoResponse.getNextBillingDate()));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(myActivity, "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                myActivity.onBackPressed();
                progress.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onDetach() {
        super.onDetach();
        myActivity = null;
    }

}