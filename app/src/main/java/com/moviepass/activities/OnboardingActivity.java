package com.moviepass.activities;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;

import org.parceler.Parcels;

public class OnboardingActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    View mRedView;
    Button mButtonSignUp;
    Button mButtonLogIn;
    TextView mNotReady;

    ImageView zero, one, two, three;
    private ViewPager mViewPager;

    final ArgbEvaluator evaluator = new ArgbEvaluator();
    ImageView[] indicators;

    int page = 0;

    CoordinatorLayout mCoordinator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mCoordinator = findViewById(R.id.main_content);
        mRedView = findViewById(R.id.red);
        mButtonLogIn = findViewById(R.id.button_log_in);
        mButtonSignUp = findViewById(R.id.button_sign_up);
//        mNotReady = findViewById(R.id.not_ready);

        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mRedView.setVisibility(View.INVISIBLE);

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

        mButtonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OnboardingActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });

        mButtonSignUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (Build.VERSION.SDK_INT >= 21 ) {

                    mRedView.bringToFront();

                    int[] location = new int[2];
                    view.getLocationOnScreen(location);

                    int cx = location[0] + view.getWidth() / 2;
                    int cy = location[1] + view.getHeight() / 2;

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
                            Intent intent = new Intent(OnboardingActivity.this, SignUpFirstOpenActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                        }
                    });
                } else {
                    Intent intent = new Intent(OnboardingActivity.this, SignUpFirstOpenActivity.class);
                    startActivity(intent);
                }


                return false;
            }
        });

        /* mNotReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OnboardingActivity.this, BrowseActivity.class);
                startActivity(intent);
            }
        }); */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_onboarding, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Share your location";
                case 1:
                    return "Get notified";
                case 2:
                    return "Choose your movie";
                case 3:
                    return "Grab your seat";
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

    public boolean shouldAskForPermissions() {
        return Build.VERSION.SDK_INT >= 23;
    }

    public void askPermissions() {
        /* TODO : ASK PERMISSIONS PLEASE */
    }
}
