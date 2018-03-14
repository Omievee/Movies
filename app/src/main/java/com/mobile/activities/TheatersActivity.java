package com.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.mobile.UserPreferences;
import com.mobile.fragments.TheatersFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Theater;
import com.moviepass.R;

import org.parceler.Parcels;

/**
 * Created by anubis on 8/4/17.
 */

public class TheatersActivity extends BaseActivity{

    public static final String THEATER = "cinema";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theaters);

        TheatersFragment theatersFragment = new TheatersFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, theatersFragment).commit();

        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

//
//    @Override
//    public void onTheaterSelect(int pos, Theater theater, int cx, int cy) {
//
//        Intent intent = new Intent(TheatersActivity.this, TheaterActivity.class);
//        intent.putExtra(THEATER, Parcels.wrap(theater));
//        startActivity(intent);
//    }

    int getContentViewId() {
        return R.layout.activity_theaters;
    }

    int getNavigationMenuItemId() {
        return R.id.action_theaters;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    if (UserPreferences.getUserId() == 0) {
                        Intent intent = new Intent(TheatersActivity.this, LogInActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(TheatersActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(TheatersActivity.this, MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(TheatersActivity.this, SettingsActivity.class));
                }
            }
        }, 0);
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }


}

//TODO Bring back animations when polished.. Simpler ones?
//        if (Build.VERSION.SDK_INT >= 21) {
//            redView.bringToFront();
//
//            int cxFinal = redView.getWidth() / 2;
//            int cyFinal = redView.getHeight() / 2;
//
//            float finalRadius = (float) Math.hypot(cxFinal, cyFinal);
//
//           Animator anim =
//                   ViewAnimationUtils.createCircularReveal(redView, cx, cy, 0, finalRadius);
//
//            redView.setVisibility(View.VISIBLE);
//            anim.start();
//            anim.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animator) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animator) {
//                    Intent intent = new Intent(TheatersActivity.this, TheaterActivity.class);
//                    intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(finalTheater));
//
//                    startActivity(intent);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animator) {
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animator) {
//                }
//            });
//        }
