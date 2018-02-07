package com.mobile.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import com.helpshift.support.Metadata;
import com.helpshift.support.Support;
import com.helpshift.util.HelpshiftContext;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.fragments.LegalFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.User;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anubis on 6/9/17.
 */

public class SettingsActivity extends BaseActivity {
    protected BottomNavigationView bottomNavigationView;

    LegalFragment legalFragment = new LegalFragment();
    boolean pushValue;
    RelativeLayout help;
    RelativeLayout signout, legal;
    Switch pushSwitch;
    TextView version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        bottomNavigationView = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        final String versionName = BuildConfig.VERSION_NAME;


        legal = findViewById(R.id.Legal);
        version = findViewById(R.id.VERSIOn);
        pushSwitch = findViewById(R.id.PushSwitch);
        help = findViewById(R.id.HELP);
        signout = findViewById(R.id.SIGNOUT);
        version.setText("App Version: " + versionName);

        fadeIn(legal);
        fadeIn(help);
        fadeIn(signout);
        fadeIn(version);
        fadeIn(pushSwitch);


        help.setOnClickListener(v -> {
            Map<String, String[]> customIssueFileds = new HashMap<>();
            customIssueFileds.put("version name", new String[]{"sl", versionName});
            String[] tags = new String[]{versionName};
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("version", versionName);
            Metadata meta = new Metadata(userData, tags);

            ApiConfig apiConfig = new ApiConfig.Builder()
                    .setEnableContactUs(Support.EnableContactUs.AFTER_VIEWING_FAQS)
                    .setGotoConversationAfterContactUs(true)
                    .setRequireEmail(false)
                    .setCustomIssueFields(customIssueFileds)
                    .setCustomMetadata(meta)
                    .setEnableTypingIndicator(true)
                    .setShowConversationResolutionQuestion(false)
                    .build();

            Support.showFAQs(SettingsActivity.this, apiConfig);
        });
        if (UserPreferences.getPushPermission()) {
            pushSwitch.setChecked(true);
        } else {
            pushSwitch.setChecked(false);
        }

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPreferences.clearUserId();
                UserPreferences.clearFbToken();
                HelpshiftContext.getCoreApi().logout();
                Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        pushSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pushSwitch.isChecked()) {
                    UserPreferences.setPushPermission(true);
                    pushValue = true;
                } else {
                    UserPreferences.setPushPermission(false);
                    pushValue = false;
                }


                //SEND isChecked TO TAPLYTICS
                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put("pushPermission", pushValue);
                    Taplytics.setUserAttributes(attributes);
                    HelpshiftContext.getCoreApi().login(String.valueOf(UserPreferences.getUserId()), UserPreferences.getUserName(), UserPreferences.getUserEmail());
                } catch (JSONException e) {

                }
            }
        });


        legal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.settingsContainer, legalFragment);
                transaction.addToBackStack(null);
                transaction.commit();
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

//        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                //open browser or intent here
//
//                HashMap config = new HashMap ();
//                config.put("gotoConversationAfterContactUs", true);
//                config.put("hideNameAndEmail", true);
//                config.put("showSearchOnNewConversation", true);
//
//                Support.showConversation(getActivity(), config);
//
//                return true;
//            }
//        });
