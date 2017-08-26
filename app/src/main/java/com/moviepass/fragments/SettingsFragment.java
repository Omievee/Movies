package com.moviepass.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.helpshift.support.ApiConfig;
import com.helpshift.support.Support;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by anubis on 5/31/17.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preferences);

        final SwitchPreference pushSwitch = (SwitchPreference) findPreference("push");
        boolean pushStatus = UserPreferences.getPushPermission();
        pushSwitch.setChecked(pushStatus);

        pushSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference arg0, Object isPushOnObject) {
                boolean pushValue = (Boolean) isPushOnObject;

                UserPreferences.setPushPermission(pushValue);

                //SEND isChecked TO TAPLYTICS
                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put("pushPermission", pushValue);
                    Taplytics.setUserAttributes(attributes);
                } catch (JSONException e){
                }

                Log.d("push", "pushValue: " + UserPreferences.getPushPermission());


                return true;
            }
        });

        String versionName = BuildConfig.VERSION_NAME;
        Preference versionPreference = findPreference("version");
        versionPreference.setSummary(versionName);


        Preference faqs = findPreference("faqs");

        faqs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //open browser or intent here

                ApiConfig apiConfig = new ApiConfig.Builder()
                        .setEnableContactUs(Support.EnableContactUs.ALWAYS)
                        .build();

                Support.showFAQs(getActivity(), apiConfig);

                return true;
            }
        });

        Preference contact = findPreference("contact_support");

        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //open browser or intent here

                HashMap config = new HashMap ();
                config.put("gotoConversationAfterContactUs", true);
                config.put("hideNameAndEmail", true);
                config.put("showSearchOnNewConversation", true);

                Support.showConversation(getActivity(), config);

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
