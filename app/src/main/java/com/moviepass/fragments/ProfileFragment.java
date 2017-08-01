package com.moviepass.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.BrowseActivity;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends PreferenceFragment {

    ProfilePlanInformationFragment profilePlanInformationFragent = new ProfilePlanInformationFragment();
    ProfileBillingAddressFragment profileBillingAddressFragment = new ProfileBillingAddressFragment();
    ProfileShippingAddressFragment profileShippingAddressFragment = new ProfileShippingAddressFragment();
    ProfilePaymentInformationFragment profilePaymentInformationFragment = new ProfilePaymentInformationFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_preferences);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");

        Preference planInformation = findPreference("plan_information");
        planInformation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profilePlanInformationFragent);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference billingAddress = findPreference("billing_address");
        billingAddress.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileBillingAddressFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference shippingAddress = findPreference("shipping_address");
        shippingAddress.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileShippingAddressFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference paymentInformation = findPreference("payment_information");
        paymentInformation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profilePaymentInformationFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference moviepassCard = findPreference("moviepass_card");
        moviepassCard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileBillingAddressFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference facebook = findPreference("facebook");
        facebook.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileBillingAddressFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference logOut = findPreference("log_out");
        logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                UserPreferences.clearUserId();

                Intent intent = new Intent(getActivity(), BrowseActivity.class);
                startActivity(intent);

                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // remove dividers
        View rootView = getView();
        ListView list = rootView.findViewById(android.R.id.list);
        list.setDivider(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");
    }
}
