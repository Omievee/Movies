package com.mobile.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.Interfaces.historyPosterClickListener;
import com.mobile.fragments.HistoryDetailsFragment;
import com.mobile.fragments.ProfileAccountInformation;
import com.mobile.fragments.ProfileAccountPlanAndBilling;
import com.mobile.fragments.ProfileAccountShippingInformation;
import com.mobile.fragments.ProfileCancellationFragment;
import com.mobile.fragments.ProfileFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.helpers.HistoryDetails;
import com.mobile.model.Movie;
import com.moviepass.R;

import jp.wasabeef.blurry.Blurry;


/**
 * Created by anubis on 7/23/17.
 */

public class ProfileActivity extends BaseActivity implements ProfileActivityInterface, historyPosterClickListener {

    public static final String TAG = "Found it";
    ProfileFragment profileFragment = new ProfileFragment();
    public BottomNavigationView bottomNavigationView;

    public ViewGroup CONTAINER;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        openProfileAccountInformationFragment();

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        Log.d(TAG, "onCreate: " + CONTAINER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fm = getFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                startActivity(new Intent(ProfileActivity.this, MoviesActivity.class));
            }
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    int getContentViewId() {
        return R.layout.activity_profile;
    }

    int getNavigationMenuItemId() {
        return R.id.action_profile;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(getApplicationContext(), MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(getApplicationContext(), TheatersActivity.class));
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            }
        }, 0);
        return true;
    }

    //} else if (itemId == R.id.action_reservations) {
//    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }


    @Override
    public void openProfileAccountInformationFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.profile_container, profileFragment);
        transaction.commit();
    }

    @Override
    public void openProfileAccountShippingInformation() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        ProfileAccountShippingInformation shippingFragment = new ProfileAccountShippingInformation();
        transaction.replace(R.id.profile_container, shippingFragment);
        transaction.addToBackStack("");
        transaction.commit();
    }

    @Override
    public void closeFragment() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void openProfileAccountPlanAndInfo() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        ProfileAccountPlanAndBilling billingFragment = new ProfileAccountPlanAndBilling();
        transaction.replace(R.id.profile_container, billingFragment);
        transaction.addToBackStack("");
        transaction.commit();
    }

    @Override
    public void openProfileAccountInformation() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        ProfileAccountInformation accountInformation = new ProfileAccountInformation();
        transaction.replace(R.id.profile_container, accountInformation);
        transaction.addToBackStack("");
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (CONTAINER != null) {
            Blurry.delete(CONTAINER);
        }

        if (bottomNavigationView.getVisibility() == View.GONE && getFragmentManager().getBackStackEntryCount() == 0) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPosterClicked(int pos, Movie historyposter, SimpleDraweeView sharedView) {
        HistoryDetailsFragment detailsFragment = HistoryDetailsFragment.newInstance(historyposter, ViewCompat.getTransitionName(sharedView));
        detailsFragment.setSharedElementEnterTransition(new HistoryDetails());
        detailsFragment.setEnterTransition(new Fade());
        detailsFragment.setExitTransition(new Fade());
        detailsFragment.setSharedElementReturnTransition(new HistoryDetails());

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.profile_container, detailsFragment);
        transaction.addToBackStack("");
        transaction.commit();

        Log.d(TAG, "onPosterClicked: " + getSupportFragmentManager().getBackStackEntryCount());

    }

    @Override
    public void openCancellationFragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        ProfileCancellationFragment cancelSubscription = new ProfileCancellationFragment();
        transaction.replace(R.id.profile_container, cancelSubscription);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
