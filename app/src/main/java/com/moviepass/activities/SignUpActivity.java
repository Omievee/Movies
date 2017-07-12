package com.moviepass.activities;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.fragments.SignUpStepFourFragment;
import com.moviepass.fragments.SignUpStepOneFragment;
import com.moviepass.fragments.SignUpStepThreeFragment;
import com.moviepass.fragments.SignUpStepTwoFragment;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    final ArgbEvaluator evaluator = new ArgbEvaluator();

    ImageView zero, one, two, three;
    ImageView[] indicators;

    int page = 0;

    CoordinatorLayout mCoordinator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mCoordinator = findViewById(R.id.main_content);

        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        indicators = new ImageView[]{zero, one, two, three};

        indicators = new ImageView[]{zero, one, two, three};

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
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
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

        ImageView img;

        int[] bgs = new int[] {R.drawable.image_onboarding_1, R.drawable.image_onboarding_2,
                R.drawable.image_onboarding_3, R.drawable.image_onboarding_4};

        int[] headers = new int[] {R.string.activity_onboarding_header_1, R.string.activity_onboarding_header_2,
                R.string.activity_onboarding_header_3, R.string.activity_onboarding_header_4};

        int[] bodies = new int[] {R.string.activity_onboarding_body_1, R.string.activity_onboarding_body_2,
                R.string.activity_onboarding_body_3, R.string.activity_onboarding_body_4};

        public static OnboardingActivity.PlaceholderFragment newInstance(int sectionNumber) {
            OnboardingActivity.PlaceholderFragment fragment = new OnboardingActivity.PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);

            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(headers[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            TextView bodyText = rootView.findViewById(R.id.section_body);
            bodyText.setText(bodies[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            img = rootView.findViewById(R.id.section_img);
            img.setBackgroundResource(bgs[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

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
                    fragment = SignUpStepOneFragment.newInstance("FirstFragment, Instance 1");
                    break;
                case 1:
                    fragment = SignUpStepTwoFragment.newInstance("SecondFragment, Instance 1");
                    break;
                case 2:
                    fragment = SignUpStepThreeFragment.newInstance("ThirdFragment, Instance 1");
                    break;
                case 3:
                    fragment = SignUpStepFourFragment.newInstance("ThirdFragment, Instance 2");
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

}
