package com.moviepass.fragments;

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
 * Created by anubis on 8/1/17.
 */

public class ProfilePlanInformationFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_plan_information_preferences);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Plan Information");

        loadUserInfo();

        /* TODO : Add Cancel & Contact Flows */
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
                    String plan = userInfoResponse.getPlan();

                    Preference planInformation = findPreference("plan_information");
                    planInformation.setSummary(plan);

                    //Bill Date
                    String nextBillingDate = userInfoResponse.getNextBillingDate();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                    try {
                        Date date = format.parse(nextBillingDate);
                        format = new SimpleDateFormat("MM/dd/yyyy");
                        nextBillingDate = format.format(date);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    Preference billDate = findPreference("bill_date");
                    billDate.setSummary(nextBillingDate);
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {

            }
        });
    }
}
