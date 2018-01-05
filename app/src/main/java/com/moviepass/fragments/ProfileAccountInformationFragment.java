package com.moviepass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.network.RestClient;
import com.moviepass.responses.UserInfoResponse;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends Fragment {

    View rootView, progress;
    ImageView downArraow, backArrow, downArrow2;
    Switch billingSwitch;
    LinearLayout shippingDetails, bilingDetails, billing2;
    TextView userName, userEmail, userAddress, userAddress2, userCity, userState, userZip, userBillingDate, userPlan, userPlanPrice, userPlanCancel, userBIllingCard,
            userBillingChange, userNewAddress, userNewAddress2, userNewCity, userNewState, userNewZip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_account_details, container, false);

        progress = rootView.findViewById(R.id.progress);
        downArraow = rootView.findViewById(R.id.DOWN);
        shippingDetails = rootView.findViewById(R.id.ShippingDetails);
        backArrow = rootView.findViewById(R.id.accountback);
        downArrow2 = rootView.findViewById(R.id.DOWN2);
        bilingDetails = rootView.findViewById(R.id.billingdetails);
        billingSwitch = rootView.findViewById(R.id.SWITCH);
        billing2 = rootView.findViewById(R.id.Billing2);

        userName = rootView.findViewById(R.id.USER_NAME);
        userEmail = rootView.findViewById(R.id.USER_EMAIL);
        userAddress = rootView.findViewById(R.id.Address1);
        userCity = rootView.findViewById(R.id.city);
        userState = rootView.findViewById(R.id.State);
        userZip = rootView.findViewById(R.id.zip);
        userAddress2 = rootView.findViewById(R.id.Address2);

        userBillingDate = rootView.findViewById(R.id.BillingDate);
        userPlan = rootView.findViewById(R.id.Plan);
        userPlanPrice = rootView.findViewById(R.id.Plan_Data);
        userPlanCancel = rootView.findViewById(R.id.PLan_cancel);
        userBIllingCard = rootView.findViewById(R.id.USER_BILLING);
        userBillingChange = rootView.findViewById(R.id.Billing_Change);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

//
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        progress.setVisibility(View.VISIBLE);

        loadUserInfo();

        downArraow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shippingDetails.getVisibility() == View.GONE) {
                    shippingDetails.setVisibility(View.VISIBLE);
                } else {
                    shippingDetails.setVisibility(View.GONE);
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        downArrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bilingDetails.getVisibility() == View.GONE) {
                    bilingDetails.setVisibility(View.VISIBLE);
                } else {
                    bilingDetails.setVisibility(View.GONE);
                }
            }
        });

        billingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (billing2.getVisibility() == View.GONE) {
                    billing2.setVisibility(View.VISIBLE);
                } else {
                    billing2.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadUserInfo() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                UserInfoResponse userInfoResponse = response.body();
                if (userInfoResponse != null) {

                    // Plan Info
                    String firstName = userInfoResponse.getUser().getFirstName();
                    String lastName = userInfoResponse.getUser().getLastName();
                    String email = userInfoResponse.getEmail();

                    userName.setText(firstName + " " + lastName);
                    userEmail.setText(email);
                    userAddress.setText(userInfoResponse.getShippingAddressLine1());

                    String address = userInfoResponse.getShippingAddressLine2();
                    List<String> addressList = Arrays.asList(address.split(",", -1));

                    for (int i = 0; i < addressList.size(); i++) {
                        Log.d(Constants.TAG, "onResponse: " + addressList.get(0));
                        userCity.setText(addressList.get(0));
                        userState.setText(addressList.get(1));
                        userZip.setText(addressList.get(2));


                    }
                    userBIllingCard.setText(userInfoResponse.getBillingCard());
                    userBillingDate.setText(userInfoResponse.getNextBillingDate());

                    Log.d(Constants.TAG, "onResponse: " + userInfoResponse.getPlan());

                    String plan = userInfoResponse.getPlan();
                    List<String> planList = Arrays.asList(plan.split(" ", -1));
                    for (int i = 0; i < planList.size(); i++) {
                        userPlan.setText(planList.get(0));
                        userPlanPrice.setText(planList.get(1));

                    }

                    progress.setVisibility(View.GONE);

                    Log.d(Constants.TAG, "onResponse: " + userInfoResponse.getShippingAddressLine1());
                    Log.d(Constants.TAG, "onResponse: " + userInfoResponse.getShippingAddressLine2());

//                    Preference namePreference = findPreference("name");
//                    namePreference.setSummary(firstName + " " + lastName);
//
//                    Preference emailPreference = findPreference("email");
//                    emailPreference.setSummary(email);
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {

            }
        });
    }
}
