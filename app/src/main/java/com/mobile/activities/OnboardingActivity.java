package com.mobile.activities;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.fragments.NearMe;
import com.mobile.fragments.WebViewFragment;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Plans;
import com.mobile.network.RestClient;
import com.mobile.responses.PlanResponse;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OnboardingActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    Button onboardingJoinNow;
    Button onboardingSignIn;

    ImageView zero, one, two, three, four;
    private ViewPager mViewPager;
    TextView nearMe;
    ImageView[] indicators;
    RelativeLayout findTheaters;
    Plans planOne, planTwo;
    ImageView ticketIconFinal;
    int page = 0;
    private TextView signUp;
    RelativeLayout mCoordinator;
    private PlanResponse planResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_onboarding);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mCoordinator = findViewById(R.id.main_content);
        onboardingSignIn = findViewById(R.id.ONBOARDING_SIGN_IN);
        onboardingJoinNow = findViewById(R.id.ONBOARDING_JOIN_NOW);

        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        indicators = new ImageView[]{zero, one, two, three};

        signUp = findViewById(R.id.signUp);

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
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
            Intent intent = new Intent(OnboardingActivity.this, LogInActivity.class);
            startActivity(intent);

            //REMOVED SIGN UP
//            if (planTwo != null) {
//                Intent intent = new Intent(OnboardingActivity.this, SignUpFirstOpenActivity.class);
//                startActivity(intent);
//            } else {
//                Intent intent = new Intent(OnboardingActivity.this, SignUpActivity.class);
//                intent.putExtra(SignUpFirstOpenActivity.SELECTED_PLAN, Parcels.wrap(planOne));
//                ProspectUser.plan = planOne;
//                startActivity(intent);
//            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                WebViewFragment web = new WebViewFragment();
                transaction.replace(R.id.fragmentContainer, web);
                transaction.addToBackStack("");
                transaction.commit();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        LogUtils.newLog("COUNT: "+fragmentManager.getBackStackEntryCount());
        WebViewFragment fragment = (WebViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (keyCode == KeyEvent.KEYCODE_BACK && fragmentManager.getBackStackEntryCount()>=1 && fragment.canGoBack()) {
            fragment.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void getPlans() {
        RestClient.getsAuthenticatedRegistrationAPI().getPlans().enqueue(new Callback<PlanResponse>() {
            @Override
            public void onResponse(Call<PlanResponse> call, Response<PlanResponse> response) {
                if (response != null && response.isSuccessful()) {
                    planResponse = response.body();
                    planOne = planResponse.getPlans().get(0);
                    if (planResponse.getPlans().size() > 1)
                        planTwo = planResponse.getPlans().get(1);
                }
            }

            @Override
            public void onFailure(Call<PlanResponse> call, Throwable t) {

            }

        });
    }


    public static class PlaceholderFragment extends Fragment {
        ImageView ticketIconFinal;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        SimpleDraweeView img;
        View findTheaters;


        int[] bgs = new int[]{R.drawable.image_onboarind_0, R.drawable.image_onboarding_1,
                R.drawable.signupimage2, R.drawable.howitworks2};

        int[] headers = new int[]{R.string.activity_onboarding_header_1, R.string.activity_onboarding_header_2, R.string.activity_onboarding_header_4, R.string.activity_onboarding_header_5};

        int[] bodies = new int[]{R.string.activity_onboarding_body_1, R.string.activity_onboarding_body_2
                , R.string.activity_onboarding_body_4, R.string.activity_onboarding_body_5};


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

            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(headers[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);
            TextView bodyText = rootView.findViewById(R.id.section_body);
            bodyText.setText(bodies[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);
            ImageView moviepassLogo = rootView.findViewById(R.id.moviepasslogo);


            img = rootView.findViewById(R.id.section_img);
            findTheaters = rootView.findViewById(R.id.findTheaters);
            if (getArguments().getInt(ARG_SECTION_NUMBER) - 1 >1) {
                findTheaters.setVisibility(View.GONE);
            }
            if (getArguments().getInt(ARG_SECTION_NUMBER) - 1 != 0) {
                textView.setVisibility(View.VISIBLE);
                moviepassLogo.setVisibility(View.GONE);
                img.setBackgroundResource(bgs[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);
            } else {
                moviepassLogo.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                final Uri imgUrl = Uri.parse("https://a1.moviepass.com/prod/images/onboarding_step1.png");
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                        .setProgressiveRenderingEnabled(true)
                        .build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setTapToRetryEnabled(true)
                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                                super.onFinalImageSet(id, imageInfo, animatable);

                            }

                            @Override
                            public void onFailure(String id, Throwable throwable) {
                                img.setBackgroundResource(bgs[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);
                            }
                        })
                        .build();

                img.setController(controller);


                ImagePipeline pipeline = Fresco.getImagePipeline();
                pipeline.clearMemoryCaches();
                pipeline.clearDiskCaches();
            }

            findTheaters.setOnClickListener(v -> {
            NearMe fragobj = new NearMe();
            FragmentManager fm = getChildFragmentManager();
            fragobj.show(fm, "fr_nearme");
        });

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
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.activity_onboarding_header_1);
                case 1:

                    return getResources().getString(R.string.activity_onboarding_header_2);
                case 2:

                    return getResources().getString(R.string.activity_onboarding_header_4);
                case 3:
                    return getResources().getString(R.string.activity_onboarding_header_5);
            }
            return null;
        }
    }

    public void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected_no_stroke : R.drawable.indicator_unselected_no_stroke
            );
        }
    }

}
