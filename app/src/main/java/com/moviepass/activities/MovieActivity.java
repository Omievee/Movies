package com.moviepass.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.fragments.MovieReviewsFragment;
import com.moviepass.fragments.MovieShowTimesFragment;
import com.moviepass.fragments.MovieSynopsisFragment;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.model.Movie;
import com.moviepass.model.Review;
import com.moviepass.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieActivity extends MainActivity {

    public static final String MOVIE = "movie";
    public static final String SYNOPSIS = "synopsis";
    public static final String TAG = "TAG";

    Toolbar mToolbar;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;

    final int TOTAL_FRAGMENTS = 3;
    final int FRAGMENT_SHOWTIMES = 0;
    final int FRAGMENT_SYNOPSIS = 1;
    final int FRAGMENT_REVIEWS = 2;

    TextView mRating;
    TextView mRunningTime;
    TextView mRatingTomato;
    ImageView rtImage;
    ViewPager mPager;
    TabLayout mTabBar;

    RelativeLayout mSecondLineThings;
    LinearLayout mRottenTomatoRating;
    LinearLayout mRottenTomatoImage;
    LinearLayout mRatingsThings;

    AppBarLayout mAppBarLayout;

    ImageView mImageHeader;
    Movie mMovie;
    String mSynopsis;
    List<Review> mReviews;
    FrameLayout mTitleBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        supportPostponeEnterTransition();

        Bundle extras = getIntent().getExtras();
        mMovie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setTitle("TEST");

        mImageHeader = (ImageView) findViewById(R.id.header);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.parallax_appbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabBar = (TabLayout) findViewById(R.id.tab_bar);
        mPager = (ViewPager) findViewById(R.id.pager);
        mRating = (TextView) findViewById(R.id.text_rating);
        mRunningTime = (TextView) findViewById(R.id.text_running_time);
        mRatingTomato = (TextView) findViewById(R.id.text_tomato_rating);
        rtImage = (ImageView) findViewById(R.id.image_tomato);
        mTitleBarContainer = (FrameLayout) findViewById(R.id.title_bar_container);

        if (savedInstanceState == null) {
            if (extras == null) {
                mSynopsis = null;
            } else {
                mSynopsis = extras.getString("SYNOPSIS");
            }
        } else {
            mSynopsis = (String) savedInstanceState.getSerializable("SYNOPSIS");
        }

        Intent i = getIntent();
        List<Review> mReviews = new ArrayList<Review>();
        mReviews =  (List<Review>) extras.getSerializable("extra");

        mPager.setAdapter(new MoviePagerAdapter(getFragmentManager()));
        mTabBar.setupWithViewPager(mPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = extras.getString(MoviesFragment.EXTRA_MOVIE_IMAGE_TRANSITION_NAME);
            mImageHeader.setTransitionName(imageTransitionName);
        }

        Picasso.with(this)
                .load(mMovie.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(mImageHeader, new Callback() {
                    @Override
                    public void onSuccess() {
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onError() {
                        supportStartPostponedEnterTransition();
                    }
                });

        mSecondLineThings = ButterKnife.findById(this, R.id.second_line_things);
        mRatingsThings = ButterKnife.findById(findViewById(R.id.second_line_things), R.id.ratings_things);
        mRottenTomatoRating = ButterKnife.findById(findViewById(R.id.second_line_things), R.id.rotten_tomato_rating);
        mRottenTomatoImage = ButterKnife.findById(findViewById(R.id.second_line_things), R.id.rotten_tomato_image);


        //MOVIE RATING && BUFFER IF RT RATING OR  MINUTES PRESENT
        if (mMovie.getRating() == null) {
            mRatingsThings.setVisibility(View.GONE);
        } else {
            if (mMovie.getTomatoRating() != 0 && UserPreferences.getRottenTomatoesDisplay() || mMovie.getRunningTime() != 0) {
                mRating.setText(String.format(Locale.getDefault(), "%s %s", mMovie.getRating(), " "));
            } else {
                mRating.setText(String.format(Locale.getDefault(), "%s", mMovie.getRating()));
            }
        }

        //add buffer if RT rating present
        if (mMovie.getTomatoRating() != 0 && UserPreferences.getRottenTomatoesDisplay()) {
            mRunningTime.setText(String.format(Locale.getDefault(), "%s %s %s", mMovie.getRunningTime(), getText(R.string.activity_movie_running_time_minutes), " "));
        } else {
            mRunningTime.setText(String.format(Locale.getDefault(), "%s %s", mMovie.getRunningTime(), getText(R.string.activity_movie_running_time_minutes)));
        }

        //RT IMAGE & RATING SELECT
        if (mMovie.getTomatoRating() != 0 && UserPreferences.getRottenTomatoesDisplay()) {
            int resId;

            if (mMovie.getTomatoRating() > 60) {
                resId = R.drawable.icon_tomato;
                rtImage.setImageResource(resId);
                rtImage.setVisibility(View.VISIBLE);
            } else {
                resId = R.drawable.icon_splat;
                rtImage.setImageResource(resId);
                rtImage.setVisibility(View.VISIBLE);
            }

            mRatingTomato.setText((String.format(Locale.getDefault(), "%s%% %s", mMovie.getTomatoRating(), " ")));
        } else {
            mRottenTomatoRating.setVisibility(View.GONE);
            mRottenTomatoImage.setVisibility(View.GONE);
        }


        /*
        Application application = (Application) getApplication();
        mTracker = application.getDefaultTracker();

        String screenName = mMovie.getTitle();
        Log.i(TAG, "Setting screen name: " + screenName);

        mTracker.setScreenName("Movie: " + screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        */
    }

    private class MoviePagerAdapter extends FragmentPagerAdapter {
        public MoviePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TOTAL_FRAGMENTS;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case FRAGMENT_SHOWTIMES:
                    fragment = MovieShowTimesFragment.newInstance(mMovie);
                    break;
                case FRAGMENT_SYNOPSIS:
                    if (mMovie.getSynopsis() != null) {
                        fragment = MovieSynopsisFragment.newInstance(mMovie.getSynopsis());
                        break;
                    } else {
                        fragment = MovieSynopsisFragment.newInstance(mSynopsis);
                        break;
                    }
                case FRAGMENT_REVIEWS:
                    if (mMovie != null) {
                        fragment = MovieReviewsFragment.newInstance(mMovie);
                        break;
                    }
            }

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title = "";

            switch (position) {
                case FRAGMENT_SHOWTIMES:
                    title = getText(R.string.activity_movie_showtimes);
                    break;
                case FRAGMENT_SYNOPSIS:
                    title = getText(R.string.activity_movie_synopsis);
                    break;
                case FRAGMENT_REVIEWS:
                    title = getText(R.string.activity_movie_reviews);
                    break;
            }

            return title;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
