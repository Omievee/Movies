package com.moviepass.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.network.RestClient;
import com.moviepass.responses.UserInfoResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends Fragment {

    View rootView;
    ImageView downArraow;

    LinearLayout shippingDetails;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        addPreferencesFromResource(R.xml.profile_account_information_preferences);

//        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
//        toolbar.setTitle("Account Details");

        loadUserInfo();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_account_details, container, false);

        downArraow = rootView.findViewById(R.id.DOWN);
        shippingDetails = rootView.findViewById(R.id.ShippingDetails);
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
