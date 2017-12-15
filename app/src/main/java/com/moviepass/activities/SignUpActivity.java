package com.moviepass.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moviepass.R;
import com.moviepass.fragments.SignUpStepOneFragment;
import com.moviepass.fragments.SignUpStepThreeFragment;
import com.moviepass.fragments.SignUpStepTwoFragment;
import com.moviepass.model.Plan;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpActivity extends AppCompatActivity implements SignUpStepTwoFragment.OnCreditCardEntered {

    String email;
    String password;
    String zip;
    Plan mPlan;

    String firstName;
    String lastName;
    String address;
    String address2;
    String city;
    String state;
    String addressZip;
    String price;

    SectionsPagerAdapter viewpagerAdapter;
    public NonSwipeableViewPager mViewPager;

    ImageView zero, one, two;
    ImageView[] indicators;
    SignUpStepTwoFragment signUpStepTwoFragment;
    SignUpStepThreeFragment signUpStepThreeFragment;

    int page = 0;

    CoordinatorLayout mCoordinator;

    int mScrollProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_signup);

        viewpagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mCoordinator = findViewById(R.id.SIGNUP_MAINLAYOUT);

        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.MAIN_FRAGMENT_CONTAINER_SIGNUP);
        mViewPager.setAdapter(viewpagerAdapter);

        indicators = new ImageView[]{zero, one, two};

        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        zip = null;
        price = null;
        mPlan = null;

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        signUpStepTwoFragment = new SignUpStepTwoFragment();
        signUpStepThreeFragment = new SignUpStepThreeFragment();




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

    @Override
    public void OnCreditCardEntered(final String ccNum, final int ccExMonth, final int ccExYear, final String ccCVV) {
        viewpagerAdapter.OnCreditCardEntered(ccNum, ccExMonth, ccExYear, ccCVV);
        String tag = "android:switcher:" + R.id.MAIN_FRAGMENT_CONTAINER_SIGNUP + ":" + 2;
        final SignUpStepThreeFragment f = (SignUpStepThreeFragment) getSupportFragmentManager().findFragmentByTag(tag);
        f.OnCreditCardEntered(ccNum, ccExMonth, ccExYear, ccCVV);
        f.confirmCCNum.setText(" - " +ccNum.substring(12, 16) + "]");
        f.confirmSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(f.confirmTermsAgreementSwitch.isChecked()){
                    f.beginRegistration(ccNum, ccExMonth, ccExYear, ccCVV);
                }else {
                    f.makeSnackbar("You must agree to the Terms of Service.");
                }
            }
        });
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
            View rootView = inflater.inflate(R.layout.ac_signup, container, false);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */


    public class SectionsPagerAdapter extends FragmentPagerAdapter implements SignUpStepTwoFragment.OnCreditCardEntered {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment fragment = null;
            switch (pos) {
                case 0:
                    fragment = new SignUpStepOneFragment();
                    break;
                case 1:
                    fragment = new SignUpStepTwoFragment();
                    break;
                case 2:
                    fragment = new SignUpStepThreeFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public void OnCreditCardEntered(String ccNum, int ccExMonth, int ccExYear, String ccCVV) {
            signUpStepThreeFragment.OnCreditCardEntered(ccNum, ccExMonth, ccExYear, ccCVV);
        }
    }


    /* Disallow swiping */
    public static class NonSwipeableViewPager extends ViewPager {
        private boolean swipingEnabled;

        public NonSwipeableViewPager(Context context) {
            super(context);
        }

        public NonSwipeableViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            swipingEnabled = true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (swipingEnabled && detectSwipeToRight(event)) {
                return super.onTouchEvent(event);
            }

            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (swipingEnabled && detectSwipeToRight(event)) {
                return super.onInterceptTouchEvent(event);
            }

            return false;
        }

        // To enable/disable swipe
        public void setPagingEnabled(boolean enabled) {
            swipingEnabled = enabled;
        }

        // Detects the direction of swipe. Right or left.
        // Returns true if swipe is in right direction
        public boolean detectSwipeToRight(MotionEvent event) {

            int initialXValue = 0; // as we have to detect swipe to right
            final int SWIPE_THRESHOLD = 100; // detect swipe
            boolean result = false;

            try {
                float diffX = event.getX() - initialXValue;

                if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                    if (diffX > 0) {
                        // swipe from left to right detected ie.SwipeRight
                        result = false;
                    } else {
                        // swipe from right to left detected ie.SwipeLeft
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
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

    /* Fragment Two */

    public String getZip() {
        try {
            Log.d("SUG get", zip);
        } catch (Exception e) {
            Log.d("e", e.toString());
        }

        return zip;
    }

    public void setPlan(Plan plan) {
        mPlan = plan;

    }

    /* Fragment Three */

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setFirstName(String frag_name) {
        firstName = frag_name;
    }

    public void setLastName(String frag_name) {
        lastName = frag_name;
    }

    public void setAddress(String frag_address) {
        address = frag_address;
    }

    public void setAddress2(String frag_address2) {
        address2 = frag_address2;
    }

    public void setCity(String frag_city) {
        city = frag_city;
    }

    public void setState(String frag_state) {
        state = frag_state;
    }

    public void setAddressZip(String frag_addressZip) {
        addressZip = frag_addressZip;

        Log.d("currentItem", String.valueOf(mViewPager.getCurrentItem()));

    }

    public void setPage() {

        String TAG = "found";

        Log.d(TAG, "setPage: " + mViewPager.getCurrentItem());
        if (mViewPager.getCurrentItem() == 0) {
            mViewPager.setCurrentItem(1);
        } else if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(2);
        }
    }

    public String getPrice() {

        try {
            Log.d("SUG get", price);
        } catch (Exception e) {
            Log.d("e", e.toString());
        }

        return price;
    }

    /* Fragment Four */

    /* Handle Back Button Behavior */
    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(0);
        } else if (mViewPager.getCurrentItem() == 2) {
            mViewPager.setCurrentItem(1);
        } else {
            super.onBackPressed();
        }
    }


}

