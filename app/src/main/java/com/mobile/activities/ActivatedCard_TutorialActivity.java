package com.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobile.Constants;
import com.mobile.home.HomeActivity;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.moviepass.R;

import org.parceler.Parcels;


public class ActivatedCard_TutorialActivity extends BaseActivity {

    Screening screeningObject;
    String selectedShowTime;
    ImageView zero, one, two, three, four, five;
    TextView done;
    Theater theater;
    ViewPager tutorialViewPager;

    int page = 0;
    ImageView[] indicators;

    ActivatedCard_TutorialActivity.tutorialAdapter tutorialAdapter;

    LinearLayout dots;
    CoordinatorLayout activityLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_activated_card__tutorial);

        zero = findViewById(R.id.tutorial_indicator_0);
        one = findViewById(R.id.tutorial_indicator_1);
        two = findViewById(R.id.tutorial_indicator_2);
        three = findViewById(R.id.tutorial_indicator_3);
        four = findViewById(R.id.tutorial_indicator_4);
        five = findViewById(R.id.tutorial_indicator_5);
        dots = findViewById(R.id.dots);
        done = findViewById(R.id.TUTORIAL_DONE);
        tutorialViewPager = findViewById(R.id.tutorial_container);
        tutorialAdapter = new tutorialAdapter(getSupportFragmentManager());
        tutorialViewPager.setAdapter(tutorialAdapter);
        indicators = new ImageView[]{one, two, three, four, five};
        activityLayout = findViewById(R.id.TUTORIAL_MAIN_ACTIVITY);

        Intent intent = getIntent();
        if (getIntent() != null) {
            screeningObject = intent.getParcelableExtra(Constants.SCREENING);
            selectedShowTime = getIntent().getStringExtra(Constants.SHOWTIME);
            theater = Parcels.unwrap(intent.getParcelableExtra(Constants.THEATER));
        }
        done.setOnClickListener(v -> {
                Intent doneIntent = new Intent(ActivatedCard_TutorialActivity.this, HomeActivity.class);
                doneIntent.putExtra("launch", true);
                doneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(doneIntent);
        });


        tutorialViewPager.setCurrentItem(page);
        updateIndicators(page);

        tutorialViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                page = position;
                updateIndicators(page);

                switch (position) {
                    case 0:
                        dots.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        done.setVisibility(View.VISIBLE);
                        fadeIn(done);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

    }

    public void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    public class tutorialAdapter extends FragmentPagerAdapter {

        public tutorialAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ActivatedCard_TutorialActivity.PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tutorial_header_1);
                case 1:
                    return getResources().getString(R.string.tutorial_header_2);
                case 2:
                    return getResources().getString(R.string.tutorial_header_3);
                case 3:
                    return getResources().getString(R.string.tutorial_header_4);
                case 4:
                    return getResources().getString(R.string.activity_onboarding_header_final);
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        ImageView img;

        int[] tutorialImages = new int[]{R.drawable.tutorial_1, R.drawable.tutorial_2,
                R.drawable.tutorial_3, R.drawable.tutorial_4, R.drawable.tutorial_5};

        int[] tutorialHeaders = new int[]{R.string.tutorial_header_1,
                R.string.tutorial_header_2, R.string.tutorial_header_3, R.string.tutorial_header_4, R.string.activity_onboarding_header_final};

        int[] tutorialBodies = new int[]{R.string.tutorial_body_1, R.string.tutorial_body_2,
                R.string.tutorial_body_3, R.string.tutorial_body_4, R.string.tutorial_body_final};


        public static ActivatedCard_TutorialActivity.PlaceholderFragment newInstance(int sectionNumber) {
            ActivatedCard_TutorialActivity.PlaceholderFragment fragment = new ActivatedCard_TutorialActivity.PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fr_tutorial, container, false);
            RelativeLayout relativeEtick = rootView.findViewById(R.id.relfinal);
            TextView finePrint = rootView.findViewById(R.id.finePrint);
            TextView textView = rootView.findViewById(R.id.tutorial_header);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(tutorialHeaders[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);


            TextView bodyText = rootView.findViewById(R.id.tutorial_body);
            bodyText.setText(tutorialBodies[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            if (getArguments().getInt(ARG_SECTION_NUMBER) - 1 == 4) {
                relativeEtick.setVisibility(View.VISIBLE);
                finePrint.setVisibility(View.VISIBLE);
            }


            img = rootView.findViewById(R.id.tutorial_images);
            img.setVisibility(View.VISIBLE);


            img.setBackgroundResource(tutorialImages[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);


            return rootView;
        }
    }
}
