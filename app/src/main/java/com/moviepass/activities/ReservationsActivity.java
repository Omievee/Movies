//package com.moviepass.activities;
//
//import android.support.v4.app.FragmentTransaction;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.design.widget.BottomNavigationView;
//import android.support.design.widget.TabLayout;
//import android.support.v4.app.Fragment;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.ActionBar;
//import android.support.v7.widget.Toolbar;
//import android.view.Menu;
//import android.view.MenuItem;
//
//import com.moviepass.R;
//import com.moviepass.fragments.HistoryFragment;
//import com.moviepass.helpers.BottomNavigationViewHelper;
//
///**
// * Created by anubis on 6/9/17.
// */
//
//public class ReservationsActivity extends BaseActivity {
//
//    private TabLayout tabLayout;
//    private ViewPager viewPager;
//    protected BottomNavigationView bottomNavigationView;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_reservations);
//
//        /* TODO: Set up active reservation later
//        viewPager = findViewById(R.id.pager);
//        setupViewPager(viewPager);
//
//        tabLayout = findViewById(R.id.tab_layout);
//        tabLayout.setupWithViewPager(viewPager);
//        setupViewPager(viewPager); */
//
//        final Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        final ActionBar actionBar = getSupportActionBar();
//
//        // Enable the Up button
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle("History");
//
//        Fragment historyFragment = new HistoryFragment();
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(R.id.container, historyFragment).commit();
//
//        bottomNavigationView = findViewById(R.id.navigation);
//        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
//        bottomNavigationView.setOnNavigationItemSelectedListener(this);
//    }
//
//    /*
//    private void setupViewPager(ViewPager viewPager){
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(new ActiveReservationFragment(), "Active");
//        adapter.addFragment(new HistoryFragment(), "History");
//        viewPager.setAdapter(adapter);
//    }
//
//    private class ViewPagerAdapter extends FragmentPagerAdapter {
//        private final List<Fragment> mFragmentList = new ArrayList<>();
//        private final List<String> mFragmentTitleList = new ArrayList<>();
//
//        private ViewPagerAdapter(FragmentManager manager) {
//            super(manager);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return mFragmentList.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return mFragmentList.size();
//        }
//
//        private void addFragment(Fragment fragment, String title) {
//            mFragmentList.add(fragment);
//            mFragmentTitleList.add(title);
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
//        }
//    }
//    */
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        updateNavigationBarState();
//    }
//
//    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
//    @Override
//    public void onPause() {
//        super.onPause();
//        overridePendingTransition(0, 0);
//    }
//
//    int getContentViewId() {
//        return R.layout.activity_reservations;
//    }
//
//    int getNavigationMenuItemId() {
//        return R.id.action_reservations;
//    }
//
//    @Override
//    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
//        bottomNavigationView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int itemId = item.getItemId();
//                if (itemId == R.id.action_profile) {
//                    startActivity(new Intent(ReservationsActivity.this, ProfileActivity.class));
//                } else if (itemId == R.id.action_reservations) {
//                } else if (itemId == R.id.action_movies) {
//                    startActivity(new Intent(ReservationsActivity.this, MoviesActivity.class));
//                } else if (itemId == R.id.action_theaters) {
//                    startActivity(new Intent(ReservationsActivity.this, TheatersActivity.class));
//                } else if (itemId == R.id.action_settings) {
//                    startActivity(new Intent(ReservationsActivity.this, SettingsActivity.class));
//                }
//            }
//        }, 300);
//        return true;
//    }
//
//    private void updateNavigationBarState(){
//        int actionId = getNavigationMenuItemId();
//        selectBottomNavigationBarItem(actionId);
//    }
//
//    void selectBottomNavigationBarItem(int itemId) {
//        Menu menu = bottomNavigationView.getMenu();
//        for (int i = 0, size = menu.size(); i < size; i++) {
//            MenuItem item = menu.getItem(i);
//            boolean shouldBeChecked = item.getItemId() == itemId;
//            if (shouldBeChecked) {
//                item.setChecked(true);
//                break;
//            }
//        }
//    }
//
//}