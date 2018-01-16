package com.moviepass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.helpshift.support.ApiConfig;
import com.helpshift.support.Support;
import com.moviepass.BuildConfig;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.fragments.SettingsFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;

/**
 * Created by anubis on 6/9/17.
 */

public class SettingsActivity extends BaseActivity {

    SettingsFragment settingsFragment = new SettingsFragment();
    protected BottomNavigationView bottomNavigationView;

    RelativeLayout help;
    RelativeLayout signout;
    Switch pushSwitch;
    TextView version;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);


        String versionName = BuildConfig.VERSION_NAME;
        version = findViewById(R.id.VERSIOn);
        pushSwitch = findViewById(R.id.PushSwitch);
        help = findViewById(R.id.HELP);
        signout = findViewById(R.id.SIGNOUT);
        version.setText("App Version: " + versionName);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApiConfig apiConfig = new ApiConfig.Builder()
                        .setEnableContactUs(Support.EnableContactUs.AFTER_VIEWING_FAQS)
                        .build();

                Support.showFAQs(SettingsActivity.this, apiConfig);
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPreferences.clearUserId();
                UserPreferences.clearFbToken();

                Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        pushSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pushSwitch.isChecked()) {
                    UserPreferences.setPushPermission(false);
                } else {
                    UserPreferences.setPushPermission(true);
                }
                Log.d(Constants.TAG, "onCreate: " + pushSwitch.isChecked());


//                //SEND isChecked TO TAPLYTICS
//                try {
//                    JSONObject attributes = new JSONObject();
//                    attributes.put("pushPermission", pushValue);
//                    Taplytics.setUserAttributes(attributes);
//                } catch (JSONException e){
//                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(SettingsActivity.this, MoviesActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    int getContentViewId() {
        return R.layout.activity_settings;
    }

    int getNavigationMenuItemId() {
        return R.id.action_settings;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.action_movies) {
                    startActivity(new Intent(SettingsActivity.this, MoviesActivity.class));
                } else if (itemId == R.id.action_theaters) {
                    startActivity(new Intent(SettingsActivity.this, TheatersActivity.class));
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                }
            }
        }, 300);
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