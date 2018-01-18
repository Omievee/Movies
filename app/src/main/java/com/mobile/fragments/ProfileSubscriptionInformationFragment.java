package com.mobile.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.helpshift.support.Support;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 8/1/17.
 */

public class ProfileSubscriptionInformationFragment extends PreferenceFragment {

    ProfileCancellationFragment profileCancellationFragment = new ProfileCancellationFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_subscription_information_preferences);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Subscription Information");

        loadUserInfo();

        /* TODO : Add Contact Flows */
        Preference cancel = findPreference("cancel");
        cancel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileCancellationFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference contact = findPreference("contact");
        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                HashMap config = new HashMap ();
                config.put("gotoConversationAfterContactUs", true);
                config.put("hideNameAndEmail", true);
                config.put("showSearchOnNewConversation", true);

                Support.showConversation(getActivity(), config);

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Subscription Information");
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
                    if (isPendingSubscription()) {
                        billDate.setSummary(R.string.xml_subscription_information_preference_bill_date_pending);
                    } else {
                        billDate.setSummary(nextBillingDate);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {

            }
        });
    }

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }
}
