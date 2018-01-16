package com.moviepass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by anubis on 5/31/17.
 */

public class SettingsFragment extends Fragment {
    View rootView;
    TextView help;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//
//        String versionName = BuildConfig.VERSION_NAME;
//        Preference versionPreference = findPreference("version");
//        versionPreference.setSummary(versionName);
//
//
//        Preference faqs = findPreference("faqs");
//        faqs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                //open browser or intent here
//
//                ApiConfig apiConfig = new ApiConfig.Builder()
//                        .setEnableContactUs(Support.EnableContactUs.AFTER_VIEWING_FAQS)
//                        .build();
//
//                Support.showFAQs(getActivity(), apiConfig);
//                return true;
//            }
//        });
//
//        Preference contact = findPreference("contact_support");
//
////        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
////            public boolean onPreferenceClick(Preference preference) {
////                //open browser or intent here
////
////                HashMap config = new HashMap ();
////                config.put("gotoConversationAfterContactUs", true);
////                config.put("hideNameAndEmail", true);
////                config.put("showSearchOnNewConversation", true);
////
////                Support.showConversation(getActivity(), config);
////
////                return true;
////            }
////        });
//
//
//        Preference logOut = findPreference("log_out");
//        logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                UserPreferences.clearUserId();
//                UserPreferences.clearFbToken();
//
//                Intent intent = new Intent(getActivity(), LogInActivity.class);
//                startActivity(intent);
//                getActivity().finish();
//
//                return true;
//            }
//        });
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        // remove dividers
//        View rootView = getView();
//        ListView list = rootView.findViewById(android.R.id.list);
//        list.setDivider(null);
//    }
    }


}

