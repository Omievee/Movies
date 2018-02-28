package com.mobile.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.helpshift.support.ApiConfig;
import com.helpshift.support.Metadata;
import com.helpshift.support.Support;
import com.helpshift.util.HelpshiftContext;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivateMoviePassCard;
import com.mobile.activities.ActivatedCard_TutorialActivity;
import com.mobile.activities.LogInActivity;
import com.mobile.activities.ProfileActivity;
import com.mobile.activities.SettingsActivity;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends Fragment {

    ProfileAccountInformationFragment profileAccountInformationFragment = new ProfileAccountInformationFragment();
    PastReservations pastReservations = new PastReservations();
    PendingReservationFragment pendingReservationFragment = new PendingReservationFragment();
    View root;
    RelativeLayout details, history, currentRes, howToUse, help;
    TextView version, TOS, PP, signout;
    Switch pushSwitch;
    boolean pushValue;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fr_profile, container, false);

        details = root.findViewById(R.id.Details);
        history = root.findViewById(R.id.History);
        currentRes = root.findViewById(R.id.Current);
        howToUse = root.findViewById(R.id.HowTO);
        help = root.findViewById(R.id.HELP);
        version = root.findViewById(R.id.VERSIOn);
        pushSwitch = root.findViewById(R.id.PushSwitch);
        TOS = root.findViewById(R.id.TOS);
        PP = root.findViewById(R.id.PP);
        signout = root.findViewById(R.id.SignOut);
        fadeIn(root);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String versionName = BuildConfig.VERSION_NAME;
        version.setText("App Version: " + versionName);

        final String ppURL = "https://www.moviepass.com/privacy/";
        final String tosURL = "https://www.moviepass.com/terms";

        if (UserPreferences.getPushPermission()) {
            pushSwitch.setChecked(true);
        } else {
            pushSwitch.setChecked(false);
        }

        pushSwitch.setOnClickListener(v -> {
            if (pushSwitch.isChecked()) {
                UserPreferences.setPushPermission(true);
                pushValue = true;
            } else {
                UserPreferences.setPushPermission(false);
                pushValue = false;
            }

            try {
                JSONObject attributes = new JSONObject();
                attributes.put("pushPermission", pushValue);
                Taplytics.setUserAttributes(attributes);
                HelpshiftContext.getCoreApi().login(String.valueOf(UserPreferences.getUserId()), UserPreferences.getUserName(), UserPreferences.getUserEmail());
            } catch (JSONException e) {

            }

        });



        signout.setOnClickListener(view16 -> {
            UserPreferences.clearUserId();
            UserPreferences.clearFbToken();
            HelpshiftContext.getCoreApi().logout();
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            startActivity(intent);
            getActivity().finishAffinity();
        });
        details.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, profileAccountInformationFragment);
            transaction.addToBackStack(null);
            transaction.commit();

        });

        TOS.setOnClickListener(view13 -> {

            Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tosURL));
            getActivity().startActivity(notifIntent);
        });

        PP.setOnClickListener(view14 -> {

            Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ppURL));
            getActivity().startActivity(notifIntent);
        });

        help.setOnClickListener(view12 -> {
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

            Support.showFAQs(getActivity(), apiConfig);
        });

        history.setOnClickListener(view2 -> {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, pastReservations);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        currentRes.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, pendingReservationFragment);
            transaction.addToBackStack(null);
            transaction.commit();

        });

        howToUse.setOnClickListener(view15 -> {
            Intent intent = new Intent(getActivity(), ActivatedCard_TutorialActivity.class);
            startActivity(intent);
        });

    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }
}