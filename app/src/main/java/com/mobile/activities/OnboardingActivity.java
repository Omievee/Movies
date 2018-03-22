package com.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.Constants;
import com.mobile.fragments.NearMe;
import com.moviepass.R;


public class OnboardingActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    Button onboardingJoinNow;
    Button onboardingSignIn;

    ImageView zero, one, two, three, four;
    private ViewPager mViewPager;
    TextView nearMe;
    ImageView[] indicators;

    int page = 0;

    CoordinatorLayout mCoordinator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_onboarding);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mCoordinator = findViewById(R.id.main_content);
        onboardingSignIn = findViewById(R.id.ONBOARDING_SIGN_IN);
        onboardingJoinNow = findViewById(R.id.ONBOARDING_JOIN_NOW);
        nearMe = findViewById(R.id.NearMe);
        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);
        four = findViewById(R.id.intro_indicator_4);

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        indicators = new ImageView[]{zero, one, two, three, four};

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        nearMe.setOnClickListener(v -> {
            NearMe fragobj = new NearMe();
            FragmentManager fm = getSupportFragmentManager();
            fragobj.show(fm, "fr_nearme");
        });


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
                    case 4:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }


        });

        onboardingSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(OnboardingActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        onboardingJoinNow.setOnClickListener(view -> {
            Intent intent = new Intent(OnboardingActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        ImageView img;

        int[] bgs = new int[]{R.drawable.image_onboarind_0, R.drawable.image_onboarding_1, R.drawable.image_onboarding_3,
                R.drawable.signupimage2, R.drawable.howitworks2};

        int[] headers = new int[]{R.string.activity_onboarding_header_1, R.string.activity_onboarding_header_2,
                R.string.activity_onboarding_header_3, R.string.activity_onboarding_header_4, R.string.activity_onboarding_header_5};

        int[] bodies = new int[]{R.string.activity_onboarding_body_1, R.string.activity_onboarding_body_2,
                R.string.activity_onboarding_body_3, R.string.activity_onboarding_body_4, R.string.activity_onboarding_body_5};


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);

            Log.d(Constants.TAG, "onCreateView: " );
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.activity_onboarding_header_1);
                case 1:
                    return getResources().getString(R.string.activity_onboarding_header_2);
                case 2:
                    return getResources().getString(R.string.activity_onboarding_header_3);
                case 3:
                    return getResources().getString(R.string.activity_onboarding_header_4);
                case 4:
                    return getResources().getString(R.string.activity_onboarding_header_5);
            }
            return null;
        }
    }

    public void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

}
