package com.moviepass.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.LogInActivity;
import com.moviepass.network.RestClient;
import com.moviepass.requests.FacebookLinkRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends PreferenceFragment {

    ProfilePlanInformationFragment profilePlanInformationFragent = new ProfilePlanInformationFragment();
    ProfileBillingAddressFragment profileBillingAddressFragment = new ProfileBillingAddressFragment();
    ProfileShippingAddressFragment profileShippingAddressFragment = new ProfileShippingAddressFragment();
    ProfilePaymentInformationFragment profilePaymentInformationFragment = new ProfilePaymentInformationFragment();
    ProfileMoviePassCardFragment profileMoviePassCardFragment = new ProfileMoviePassCardFragment();

    CallbackManager callbackManager;
    LoginButton loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_preferences);

        FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();

        /* final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Profile"); */

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
                transaction.replace(R.id.container, profileMoviePassCardFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        final Preference facebook = findPreference("facebook");

        if (UserPreferences.getFbToken().matches("token")) {
            facebook.setSummary(R.string.fragment_profile_facebook_never_post);
            facebook.setTitle(R.string.fragment_profile_facebook_connect);
        } else {
            facebook.setTitle(R.string.fragment_profile_facebook_disconnect);
        }

        facebook.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d("prefClick", "prefClick");

                final LoginButton l = new LoginButton(getActivity());
                loginButton = getActivity().findViewById(R.id.button_facebook_log_in);
                loginButton.setReadPermissions("public_profile", "email", "user_birthday");
                l.setReadPermissions("public_profile", "email", "user_birthday");

                l.performClick();
                loginButton.performClick();

                if (UserPreferences.getFbToken().matches("token")) {
                    facebook.setSummary(R.string.fragment_profile_facebook_never_post);
                    facebook.setTitle(R.string.fragment_profile_facebook_connect);
                } else {
                    facebook.setTitle(R.string.fragment_profile_facebook_disconnect);
                }

                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String fbToken = loginResult.getAccessToken().getToken();
                        Log.d("fbUserId", fbToken);
                        UserPreferences.setFbToken(fbToken);

                        if (UserPreferences.getFbToken().matches("token")) {
                            facebook.setSummary(R.string.fragment_profile_facebook_never_post);
                            facebook.setTitle(R.string.fragment_profile_facebook_connect);
                        } else {
                            facebook.setTitle(R.string.fragment_profile_facebook_disconnect);
                        }

                        FacebookLinkRequest fbLinkRequest = new FacebookLinkRequest(fbToken);
                        RestClient.getAuthenticated().linkToFacebook(fbLinkRequest).enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                Log.d("response", response.toString());

                                Toast.makeText(getActivity(), "Your Facebook account has been connected.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                                Log.d("error", t.getMessage().toString());
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d("cancel", "onCancel");

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("error", exception.toString());
                    }
                });
                return true;
            }
        });

        Preference logOut = findPreference("log_out");
        logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                UserPreferences.clearUserId();

                Intent intent = new Intent(getActivity(), LogInActivity.class);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}