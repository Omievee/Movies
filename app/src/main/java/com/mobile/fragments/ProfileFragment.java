package com.mobile.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mobile.UserPreferences;
import com.mobile.activities.LogInActivity;
import com.mobile.network.RestClient;
import com.mobile.requests.FacebookLinkRequest;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends PreferenceFragment {

    ProfileAccountInformationFragment profileAccountInformationFragment = new ProfileAccountInformationFragment();
    ProfileSubscriptionInformationFragment profileSubscriptionInformationFragment = new ProfileSubscriptionInformationFragment();
    ProfileBillingAddressFragment profileBillingAddressFragment = new ProfileBillingAddressFragment();
    ProfileShippingAddressFragment profileShippingAddressFragment = new ProfileShippingAddressFragment();
    ProfilePaymentInformationFragment profilePaymentInformationFragment = new ProfilePaymentInformationFragment();
    ProfileMoviePassCardFragment profileMoviePassCardFragment = new ProfileMoviePassCardFragment();

    CallbackManager callbackManager;
    LoginButton loginButton;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_preferences);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String fbToken = loginResult.getAccessToken().getToken();
                Log.d("fbUserId", fbToken);
                UserPreferences.setFbToken(fbToken);


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

        /* final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Profile"); */

        Preference accountInformation = findPreference("account_information");
        accountInformation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileAccountInformationFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });

        Preference subscriptionInformation = findPreference("subscription_information");
        subscriptionInformation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.container, profileSubscriptionInformationFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });


        /** CURRENT */

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

        Log.d("fbToken", UserPreferences.getFbToken());

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                String newAccessTokenString = String.valueOf(newAccessToken);
                UserPreferences.setFbToken(newAccessTokenString);
            }
        };

        final Preference facebook = findPreference("facebook");

        if (UserPreferences.getFbToken().matches("token")) {
            facebook.setSummary(R.string.fragment_profile_facebook_never_post);
            facebook.setTitle(R.string.fragment_profile_facebook_connect);
        } else {
            facebook.setTitle(R.string.fragment_profile_facebook_disconnect);
        }

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };


        try {
            Log.d("accessToken", String.valueOf(accessToken));
        } catch (Exception e) {
        }

        try {
            Log.d("accessTokenToken", accessToken.getToken());
        } catch (Exception e) {
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

                return true;
            }
        });

        Preference logOut = findPreference("log_out");
        logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                UserPreferences.clearUserId();
                UserPreferences.clearFbToken();
                Intent intent = new Intent(getActivity(), LogInActivity.class);
                startActivity(intent);
                getActivity().finish();
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

//        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
//        toolbar.setTitle("Profile");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}