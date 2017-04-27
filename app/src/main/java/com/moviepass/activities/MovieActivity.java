package com.moviepass.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
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
import com.moviepass.model.Movie;
import com.moviepass.model.Review;
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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setTitle("TEST");

        mMovie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE));


        //ButterKnife.bind(this);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.parallax_appbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabBar = (TabLayout) findViewById(R.id.tabbar);
        mPager = (ViewPager) findViewById(R.id.pager);
        mRating = (TextView) findViewById(R.id.textRating);
        mRunningTime = (TextView) findViewById(R.id.textRunningTime);
        mRatingTomato = (TextView) findViewById(R.id.textRatingTomato);
        rtImage = (ImageView) findViewById(R.id.rtImage);
        mTitleBarContainer = (FrameLayout) findViewById(R.id.title_bar_container);
        mImageHeader = (ImageView) findViewById(R.id.header);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mSynopsis = null;
            } else {
                mSynopsis = extras.getString("SYNOPSIS");
            }
        } else {
            mSynopsis = (String) savedInstanceState.getSerializable("SYNOPSIS");
        }


        Intent i = getIntent();
        Bundle extras = i.getExtras();
        List<Review> mReviews = new ArrayList<Review>();
        mReviews =  (List<Review>) extras.getSerializable("extra");

        mPager.setAdapter(new MoviePagerAdapter(getFragmentManager()));
        mTabBar.setupWithViewPager(mPager);

        Picasso.with(this).load(mMovie.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(mImageHeader);

        mSecondLineThings = ButterKnife.findById(this, R.id.secondLineThings);
        mRottenTomatoRating = ButterKnife.findById(findViewById(R.id.secondLineThings), R.id.rottenTomatoRating);
        mRottenTomatoImage = ButterKnife.findById(findViewById(R.id.secondLineThings), R.id.rottenTomatoImage);
        mRatingsThings = ButterKnife.findById(findViewById(R.id.secondLineThings), R.id.ratingsThings);


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
            mRunningTime.setText(String.format(Locale.getDefault(), "%s %s %s", mMovie.getRunningTime(), getText(R.string.activity_movie_holder_running_time), " "));
        } else {
            mRunningTime.setText(String.format(Locale.getDefault(), "%s %s", mMovie.getRunningTime(), getText(R.string.activity_movie_holder_running_time)));
        }

        //RT IMAGE & RATING SELECT
        if (mMovie.getTomatoRating() != 0) {
            if (UserPreferences.getRottenTomatoesDisplay()) {
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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
