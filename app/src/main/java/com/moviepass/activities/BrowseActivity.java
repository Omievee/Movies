package com.moviepass.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.fragments.BrowseFragment;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.fragments.TheatersFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Theater;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by anubis on 6/9/17.
 */

public class BrowseActivity extends BaseActivity implements TheatersFragment.OnTheaterSelect {

    int SUPPORT_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    String TAG = "TAG";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    @BindView(R.id.red)
    View mRedView;

    protected BottomNavigationView bottomNavigationView;

    public static BrowseFragment newInstance() { return new BrowseFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        viewPager = findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        mRedView = findViewById(R.id.red);

        mRedView.setVisibility(View.INVISIBLE);
    }

    private void setupViewPager(ViewPager viewPager){
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            adapter.addFragment(new TheatersFragment(),"Theaters");
            adapter.addFragment(new MoviesFragment(),"Movies");
            viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRedView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTheaterSelect(int pos, Theater theater, int cx, int cy) {
        final Theater finalTheater = theater;

        if (Build.VERSION.SDK_INT >= 21) {
            mRedView.bringToFront();

            int cxFinal = mRedView.getWidth() / 2;
            int cyFinal = mRedView.getHeight() / 2;

            float finalRadius = (float) Math.hypot(cxFinal, cyFinal);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(mRedView, cx, cy, 0, finalRadius);

            mRedView.setVisibility(View.VISIBLE);
            anim.start();
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                    Intent intent = new Intent(BrowseActivity.this, TheaterActivity.class);
                    intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(finalTheater));

                    startActivity(intent);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    int getContentViewId() {
        return R.layout.activity_browse;
    }

    int getNavigationMenuItemId() {
        return R.id.action_movies;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    if (UserPreferences.getUserId() == 0) {
                        Intent intent = new Intent(BrowseActivity.this, LogInActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(BrowseActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.action_reservations) {
                    startActivity(new Intent(BrowseActivity.this, ReservationsActivity.class));
                } else if (itemId == R.id.action_movies) {
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(BrowseActivity.this, NotificationsActivity.class));
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(BrowseActivity.this, SettingsActivity.class));
                }
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState(){
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

}