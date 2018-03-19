package com.mobile.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.GsonBuilder;
import com.helpshift.util.HelpshiftContext;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.fragments.NoInternetFragment;
import com.mobile.fragments.TicketVerificationDialog;
import com.mobile.network.RestClient;
import com.mobile.responses.RestrictionsResponse;
import com.mobile.responses.UserInfoResponse;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.taplytics.sdk.Taplytics;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    int offset = 3232323;
    Bundle bundle;
    /* Permissions */
    public final static int REQUEST_LOCATION_CODE = 1000;
    public final static int REQUEST_STORAGE_CODE = 1001;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1004;
    public RestrictionsResponse restriction;
    private static String LOCATION_PERMISSIONS[] = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static String STORAGE_PERMISSIONS[] = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    UserInfoResponse userInfoResponse;
    protected BottomNavigationView bottomNavigationView;

    public String myZip;

    AlertDialog alert;
    public static final String MyPREFERENCES = "myprefs";

    /* Creation */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("email", UserPreferences.getUserEmail());
            attributes.put("name", UserPreferences.getUserName());
            attributes.put("user_id", String.valueOf(UserPreferences.getUserId()));
            Log.d("taplytics put", UserPreferences.getUserEmail());
            Taplytics.setUserAttributes(attributes);
        } catch (JSONException e) {

        }

        try {
            HelpshiftContext.getCoreApi().login(String.valueOf(UserPreferences.getUserId()), UserPreferences.getUserName(), UserPreferences.getUserEmail());
        } catch (Exception e) {

        }

        checkRestrictions();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRestrictions();
        if (!isOnline()) {
            NoInternetFragment fragobj = new NoInternetFragment();
            FragmentManager fm = getSupportFragmentManager();
            fragobj.show(fm, "fr_no_internet");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public void checkRestrictions() {
        RestClient.getAuthenticated().getRestrictions(UserPreferences.getUserId() + offset).enqueue(new Callback<RestrictionsResponse>() {
            @Override
            public void onResponse(Call<RestrictionsResponse> call, Response<RestrictionsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {

                    Log.d("LOG_IN", "onResponse: USER EMAIL: "+UserPreferences.getUserEmail());


                    restriction = response.body();
                    String status = restriction.getSubscriptionStatus();
                    boolean fbPresent = restriction.getFacebookPresent();
                    boolean threeDEnabled = restriction.get3dEnabled();
                    boolean allFormatsEnabled = restriction.getAllFormatsEnabled();
                    boolean proofOfPurchaseRequired = restriction.getProofOfPurchaseRequired();
                    boolean hasActiveCard = restriction.getHasActiveCard();
                    boolean subscriptionActivationRequired = restriction.isSubscriptionActivationRequired();

                    if (!UserPreferences.getRestrictionSubscriptionStatus().equals(status) ||
                            UserPreferences.getRestrictionFacebookPresent() != fbPresent ||
                            UserPreferences.getRestrictionThreeDEnabled() != threeDEnabled ||
                            UserPreferences.getRestrictionAllFormatsEnabled() != allFormatsEnabled ||
                            UserPreferences.getProofOfPurchaseRequired() != proofOfPurchaseRequired ||
                            UserPreferences.getRestrictionHasActiveCard() != hasActiveCard ||
                            UserPreferences.getIsSubscriptionActivationRequired() != subscriptionActivationRequired) {

                        UserPreferences.setRestrictions(status, fbPresent, threeDEnabled, allFormatsEnabled, proofOfPurchaseRequired, hasActiveCard, subscriptionActivationRequired);
                    }

                    Log.d("LOG_IN", "onResponse: USER RESTRICTION STATUS: "+UserPreferences.getRestrictionSubscriptionStatus());
                    Log.d("LOG_IN", "onResponse: USER ACTIVE CARD: "+UserPreferences.getIsSubscriptionActivationRequired());

                    //IF popInfo NOT NULL THEN INFLATE TicketVerificationActivity
                    if (UserPreferences.getProofOfPurchaseRequired() && restriction.getPopInfo() != null) {
                        int reservationId = restriction.getPopInfo().getReservationId();
                        String movieTitle = restriction.getPopInfo().getMovieTitle();
                        String tribuneMovieId = restriction.getPopInfo().getTribuneMovieId();
                        String theaterName = restriction.getPopInfo().getTheaterName();
                        String tribuneTheaterId = restriction.getPopInfo().getTribuneTheaterId();
                        String showtime = restriction.getPopInfo().getShowtime();

                        bundle = new Bundle();
                        bundle.putInt("reservationId", reservationId);
                        bundle.putString("mSelectedMovieTitle", movieTitle);
                        bundle.putString("tribuneMovieId", tribuneMovieId);
                        bundle.putString("mTheaterSelected", theaterName);
                        bundle.putString("tribuneTheaterId", tribuneTheaterId);
                        bundle.putString("showtime", showtime);


                        TicketVerificationDialog dialog = new TicketVerificationDialog();
                        FragmentManager fm = getSupportFragmentManager();
                        addFragmentOnlyOnce(fm, dialog, "fr_ticketverification_banner");
                    }

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        //IF API ERROR LOG OUT TO LOG BACK IN
                        /*
                        if (jObjError.getString("message").matches("INVALID API REQUEST")) {

                        */

                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<RestrictionsResponse> call, Throwable t) {

            }
        });
    }

    public void addFragmentOnlyOnce(FragmentManager fragmentManager, TicketVerificationDialog fragment, String tag) {
        // Make sure the current transaction finishes first
        fragmentManager.executePendingTransactions();
        // If there is no fragment yet with this tag...
        if (fragmentManager.findFragmentByTag(tag) == null) {
            TicketVerificationDialog dialog = new TicketVerificationDialog();
            dialog.setArguments(bundle);
            FragmentManager fm = getSupportFragmentManager();
            dialog.setCancelable(false);
            dialog.show(fm, "fr_ticketverification_banner");
        }
    }

    public boolean isOnline() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = connectivityManager.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }


    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        Animation animate = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }


        };

        animate.setDuration((long) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(animate);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((long) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


}


