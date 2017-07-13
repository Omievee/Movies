package com.moviepass.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.moviepass.R;
import com.moviepass.fragments.SignUpStepFourFragment;
import com.moviepass.fragments.SignUpStepOneFragment;
import com.moviepass.fragments.SignUpStepThreeFragment;
import com.moviepass.fragments.SignUpStepTwoFragment;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpActivity extends AppCompatActivity {

    String zip;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    ImageView zero, one, two, three;
    ImageView[] indicators;

    int page = 0;

    Button mNext;
    CoordinatorLayout mCoordinator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mNext = findViewById(R.id.button_next);
        mCoordinator = findViewById(R.id.main_content);

        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        indicators = new ImageView[]{zero, one, two, three};

        zip = null;

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                page = position;
                updateIndicators(page);

                switch (position) {
                    case 0:
                        mNext.setText(R.string.fragment_sign_up_step_one_next);
                        break;
                    case 1:
                        mNext.setText(R.string.fragment_sign_up_step_two_next_address);
                        break;
                    case 2:
                        mNext.setText(R.string.fragment_sign_up_step_three_next);
                        break;
                    case 3:
                        mNext.setText(R.string.fragment_sign_up_step_four_finish);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_sign_up, container, false);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment fragment = null;
            switch(pos) {
                case 0:
                    fragment = new SignUpStepOneFragment();
                    break;
                case 1:
                    fragment = new SignUpStepTwoFragment();
                    break;
                case 2:
                    fragment = new SignUpStepThreeFragment();
                    break;
                case 3:
                    fragment = new SignUpStepFourFragment();
                    break;
            }
            return fragment;
        }
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    /* Fragment One */

    public void setZip(String zipcode) {
        zip = zipcode;
        Log.d("SUA set", zip);

        mViewPager.setCurrentItem(1);
    }

    public String getZip() {
        try {
            Log.d("SUG get", zip);
        } catch (Exception e) {
            Log.d("e", e.toString());
        }

        return zip;
    }

    /* Fragment Two */

}
