package com.mobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivatedCard_TutorialActivity;
import com.mobile.activities.LogInActivity;
import com.mobile.activities.ProfileActivity;
import com.mobile.loyaltyprogram.LoyaltyProgramFragment;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends Fragment {

    ProfileAccountInformationFragment profileAccountInformationFragment = new ProfileAccountInformationFragment();
    PastReservations pastReservations = new PastReservations();
    PendingReservationFragment pendingReservationFragment = new PendingReservationFragment();
    ReferAFriend refer = new ReferAFriend();
    View root;
    RelativeLayout details, history, currentRes, howToUse, help, referAFriend, loyaltyPrograms;
    TextView version, TOS, PP, signout;
    Switch pushSwitch;
    boolean pushValue;
    Activity myActivity;
    Context myContext;
    ProfileActivityInterface listener;
    Realm historyRealm;

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
        loyaltyPrograms = root.findViewById(R.id.LoyaltyPrograms);

        referAFriend = root.findViewById(R.id.ReferAFriend);
        fadeIn(root);


        RealmConfiguration historyConfig = new RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        historyRealm = Realm.getInstance(historyConfig);
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
            historyRealm.executeTransactionAsync(realm -> realm.deleteAll());
//            UserPreferences.clearEverything();
            HelpshiftContext.getCoreApi().logout();
            Intent intent = new Intent(myActivity, LogInActivity.class);
            startActivity(intent);
            myActivity.finishAffinity();
        });


        details.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = myActivity.getFragmentManager();
            fragmentManager.popBackStack();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, profileAccountInformationFragment);
            transaction.addToBackStack("");
            transaction.commit();
            ((ProfileActivity) myActivity).bottomNavigationView.setVisibility(View.GONE);
        });

        referAFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = myActivity.getFragmentManager();
                fragmentManager.popBackStack();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
                transaction.replace(R.id.profile_container, refer);
                transaction.addToBackStack("");
                transaction.commit();
                ((ProfileActivity) myActivity).bottomNavigationView.setVisibility(View.GONE);
            }
        });

        TOS.setOnClickListener(view13 -> {

            Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tosURL));
            myActivity.startActivity(notifIntent);
        });

        PP.setOnClickListener(view14 -> {

            Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ppURL));
            myActivity.startActivity(notifIntent);
        });

        help.setOnClickListener(view12 -> {
            Map<String, String[]> customIssueFileds = new HashMap<>();
            customIssueFileds.put("version name", new String[]{"sl", versionName});
            String date = UserPreferences.getLastCheckInAttemptDate();
            String time = UserPreferences.getLastCheckInAttemptTime();
            customIssueFileds.put("lastCheckInAttemptDate", new String[]{"sl", date});
            customIssueFileds.put("lastCheckInAttemptTime", new String[]{"sl", time});

            String[] tags = new String[]{versionName};
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("version", versionName);
            userData.put("lastCheckInAttemptDate", date);
            userData.put("lastCheckInAttemptTime", time);
            Metadata meta = new Metadata(userData, tags);

            ApiConfig apiConfig = new ApiConfig.Builder()
                    .setEnableContactUs(Support.EnableContactUs.ALWAYS)
                    .setGotoConversationAfterContactUs(true)
                    .setRequireEmail(false)
                    .setCustomIssueFields(customIssueFileds)
                    .setCustomMetadata(meta)
                    .setEnableTypingIndicator(true)
                    .setShowConversationResolutionQuestion(false)
                    .build();

            Support.showFAQs(myActivity, apiConfig);
        });

        history.setOnClickListener(view2 -> {
            FragmentManager fragmentManager = myActivity.getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, pastReservations);
            transaction.addToBackStack("");
            transaction.commit();
            ((ProfileActivity) myActivity).bottomNavigationView.setVisibility(View.GONE);
        });

        currentRes.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = myActivity.getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            transaction.replace(R.id.profile_container, pendingReservationFragment);
            transaction.addToBackStack("");
            transaction.commit();
            ((ProfileActivity) myActivity).bottomNavigationView.setVisibility(View.GONE);

        });

        howToUse.setOnClickListener(view15 -> {
            Intent intent = new Intent(myActivity, ActivatedCard_TutorialActivity.class);
            startActivity(intent);
        });

        loyaltyPrograms.setOnClickListener(view1-> {
            FragmentTransaction transaction = myActivity.getFragmentManager().beginTransaction();
            transaction.replace(R.id.profile_container, LoyaltyProgramFragment.Companion.newInstance());
            transaction.addToBackStack("");
            transaction.commit();
        });

    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileActivityInterface) {
            listener = (ProfileActivityInterface) context;
        }
        myContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }
}