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
import com.mobile.helpers.GoWatchItSingleton;
import com.moviepass.R;

import java.util.List;

/**
 * Created by anubis on 8/4/17.
 */

public class TheatersActivity extends BaseActivity{

    public static final String THEATER = "cinema";
    List<String> urlPath;
    String campaign="no_campaign";
    String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theaters);

        url = "https://moviepass.com/go/map";
        if(GoWatchItSingleton.getInstance().getCampaign()!=null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + GoWatchItSingleton.getInstance().getCampaign();
        GoWatchItSingleton.getInstance().userOpenedTheaterTab(url,"map_view_click");
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

