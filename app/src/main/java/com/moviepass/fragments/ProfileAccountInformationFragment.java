package com.moviepass.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

/**
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_account_information_preferences);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Account Information");

        loadUserInfo();

    }

    @Override
    public void onResume() {
        super.onResume();

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Account Information");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // remove dividers
        View rootView = getView();
        ListView list = rootView.findViewById(android.R.id.list);
        list.setDivider(null);
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

                    Preference namePreference = findPreference("name");
                    namePreference.setSummary(firstName + " " + lastName);

                    Preference emailPreference = findPreference("email");
                    emailPreference.setSummary(email);
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {

            }
        });
    }
}
