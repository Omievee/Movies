package com.moviepass.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.ListView;

import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.BrowseActivity;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.profile_preferences);

        Preference logOut = findPreference("log_out");
        logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                UserPreferences.clearUserId();

                Intent intent = new Intent(getActivity(), BrowseActivity.class);
                startActivity(intent);

                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // remove dividers
        View rootView = getView();
        ListView list = rootView.findViewById(android.R.id.list);
        list.setDivider(null);
    }
}
