package com.mobile.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.helpshift.support.Log;
import com.mobile.fragments.ConfirmationSignUpFragment;
import com.mobile.fragments.SignUpFirstTime;
import com.mobile.fragments.SignUpStepOneFragment;
import com.mobile.fragments.SignUpStepThreeFragment;
import com.mobile.fragments.SignUpStepTwoFragment;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Plan;
import com.mobile.model.Plans;
import com.mobile.model.ProspectUser;
import com.mobile.network.RestClient;
import com.mobile.responses.PlanResponse;
import com.moviepass.R;

import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpActivity extends AppCompatActivity implements SignUpStepTwoFragment.OnCreditCardEntered {

    String email;
    String password;
    String zip;
    Plan mPlan;
    String gender;
    String dob;

    String firstName;
    String lastName;
    String address;
    String address2;
    String city;
    String state;
    String addressZip;
    String price;
     String androidID;
    Plans selectedPlan;

    SectionsPagerAdapter viewpagerAdapter;
    public NonSwipeableViewPager mViewPager;

    ImageView zero, one, two, logo,three;
    ImageView checkZero,checkOne,checkTwo,checkThree;
    RelativeLayout frame;
    ImageView[] indicators, checkMarks;
    SignUpStepOneFragment signUpStepOneFragment;
    SignUpStepTwoFragment signUpStepTwoFragment;
    SignUpStepThreeFragment signUpStepThreeFragment;
    SignUpFirstTime signUpFirstTime;
    boolean firstCompleted = false;
    boolean secondCompleted = false;
    boolean thirdCompleted = false;


    int page = 0;

    CoordinatorLayout mCoordinator;

    int mScrollProgress;
    private PlanResponse planResponse;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_signup);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewpagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mCoordinator = findViewById(R.id.SIGNUP_MAINLAYOUT);

        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);
        checkZero = findViewById(R.id.intro_indicator_0_check);
        checkOne = findViewById(R.id.intro_indicator_1_check);
        checkTwo = findViewById(R.id.intro_indicator_2_check);
        checkThree = findViewById(R.id.intro_indicator_3_check);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.MAIN_FRAGMENT_CONTAINER_SIGNUP);
        mViewPager.setAdapter(viewpagerAdapter);

        indicators = new ImageView[]{zero, one, two,three};
        checkMarks = new ImageView[]{checkZero, checkOne, checkTwo,checkThree};

        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        selectedPlan = Parcels.unwrap(getIntent().getParcelableExtra(SignUpFirstOpenActivity.SELECTED_PLAN));
        if(selectedPlan==null){
            getPlans();
        }

        zip = null;
        price = null;
        mPlan = null;

        logo = findViewById(R.id.logo);
        frame = findViewById(R.id.frame_layout);

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        signUpStepTwoFragment = new SignUpStepTwoFragment();
        signUpStepThreeFragment = new SignUpStepThreeFragment();
        signUpStepOneFragment = new SignUpStepOneFragment();
        signUpFirstTime = new SignUpFirstTime();

        mCoordinator.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = mCoordinator.getRootView().getHeight() - mCoordinator.getHeight();
            if (heightDiff > 100) { // if more than 200 dp, it's probably a keyboard...
                LogUtils.newLog("KEYBOARD", "mCOORDINATIOR ROOT VIEW HEIGHT "+mCoordinator.getRootView().getHeight());
                LogUtils.newLog("KEYBOARD", "mCOORDINATIOR  HEIGHT "+mCoordinator.getHeight());
                LogUtils.newLog("KEYBOARD", "mCOORDINATIOR  DIFFERENCE "+heightDiff);
            }
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


    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }


    public void getPlans(){
        RestClient.getsAuthenticatedRegistrationAPI().getPlans().enqueue(new Callback<PlanResponse>() {
            @Override
            public void onResponse(Call<PlanResponse> call, Response<PlanResponse> response) {
                if(response!=null && response.isSuccessful()){
                    planResponse = response.body();
                    selectedPlan = planResponse.getPlans().get(0);
                    ProspectUser.plan = selectedPlan;
                }
                else{
                    Log.d("GET PLANS", "onResponse: Error getting plans");
                }
            }

            @Override
            public void onFailure(Call<PlanResponse> call, Throwable t) {
                Log.d("GET PLANS", "onResponse: Error getting plans");
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
    public void OnCreditCardEntered(final String ccNum, final String ccExMonth, final String ccExYear, final String ccCVV) {
        viewpagerAdapter.OnCreditCardEntered(ccNum, ccExMonth, ccExYear, ccCVV);
        String tag = "android:switcher:" + R.id.MAIN_FRAGMENT_CONTAINER_SIGNUP + ":" + 3;
        final SignUpStepThreeFragment f = (SignUpStepThreeFragment) getSupportFragmentManager().findFragmentByTag(tag);
        f.OnCreditCardEntered(ccNum, ccExMonth, ccExYear, ccCVV);
        f.confirmCCNum.setText(" - " + ccNum.substring(12, 16));
        f.confirmSubmit.setOnClickListener(v -> {
            if (f.confirmTermsAgreementSwitch.isChecked()) {
                f.beginRegistration(ccNum, ccExMonth, ccExYear, ccCVV);
                f.confirmSubmit.setEnabled(false);
            } else {
                f.makeSnackbar("You must agree to the Terms of Service.");
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
            return 5;
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment fragment = null;
            switch (pos) {
                case 0:
                    fragment = new SignUpFirstTime();
                    break;
                case 1:
                    fragment = new SignUpStepOneFragment();
                    break;
                case 2:
                    fragment = new SignUpStepTwoFragment();
                    break;
                case 3:
                    fragment = new SignUpStepThreeFragment();
                    break;
                case 4:
                    fragment = new ConfirmationSignUpFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public void OnCreditCardEntered(String ccNum, String ccExMonth, String ccExYear, String ccCVV) {
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

    public void updateIndicators(int position) {
//        for (int i = 0; i < indicators.length; i++) {
//            indicators[i].setBackgroundResource(i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected);
//                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) indicators[i].getLayoutParams();
////                int margin = getResources().getDimensionPixelSize(R.dimen.left_margin_circles);
////            if(position!=i){
////                    marginParams.setMargins(margin, margin, margin, margin);
////            }
////            else {
////                if((i==0 && !firstCompleted) || (i==1 && !secondCompleted) || (i==2 && !thirdCompleted) || i==3)
////                    marginParams.setMargins(0, 0, 0, 0);
////            }
//        }
//        if(firstCompleted){
//            indicators[0].setBackgroundResource(R.drawable.sign_up_indicator_completed);
//            checkMarks[0].setVisibility(View.VISIBLE);
//        }
//        if(secondCompleted){
//            indicators[1].setBackgroundResource(R.drawable.sign_up_indicator_completed);
//            checkMarks[1].setVisibility(View.VISIBLE);
//        }
//        if(thirdCompleted){
//            indicators[2].setBackgroundResource(R.drawable.sign_up_indicator_completed);
//            checkMarks[2].setVisibility(View.VISIBLE);
//        }
    }


    /* Fragment First Open */

    public void setEmail(String email){
        this.email = email;
        Log.d("FIRST", "setEmail: "+email);
    }
    public void setPassword(String password){
        this.password = password;
        Log.d("FIRST", "setEmail: "+password);
    }
    public void setGender(String gender){
        this.gender = gender;
        Log.d("FIRST", "setEmail: "+gender);
    }
    public void setDOB(String dob){
        this.dob = dob;
        Log.d("FIRST", "setEmail: "+dob);
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
     Log.d("FIRST", "setAndroidID: " + androidID);
    }

    /* Fragment One */

    public void setZip(String zipcode) {
        zip = zipcode;
        mViewPager.setCurrentItem(1);
    }

    /* Fragment Two */

    public String getZip() {
        try {
        } catch (Exception e) {
        }

        return zip;
    }

    public String getPlanPrice(){
        return selectedPlan.getPrice();
    }

    public String getPaymentDisclaimer(){
        return  selectedPlan.getPaymentDisclaimer();
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

    }

    public void confirmFirstStep (){
        firstCompleted = true;
    }
    public void confirmSecondStep(){
        secondCompleted = true;
    }

    public void confirmThirdStep(){
        thirdCompleted = true;
    }

    public void setPage() {
        String TAG = "found";
        if (mViewPager.getCurrentItem() == 0) {
            firstCompleted=true;
            mViewPager.setCurrentItem(1);
        } else if (mViewPager.getCurrentItem() == 1) {
            secondCompleted=true;
            mViewPager.setCurrentItem(2);
        } else if (mViewPager.getCurrentItem() == 2) {
            thirdCompleted=true;
            mViewPager.setCurrentItem(3);
        } else if(mViewPager.getCurrentItem() == 3){
            mViewPager.setCurrentItem(4);

        }
    }

    public String getPrice() {

        try {
        } catch (Exception e) {
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
        } else if(mViewPager.getCurrentItem() == 3){
            mViewPager.setCurrentItem(2);
        } else{
            super.onBackPressed();
        }
    }


}

