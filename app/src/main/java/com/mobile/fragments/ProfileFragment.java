package com.mobile.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.helpshift.support.ApiConfig;
import com.helpshift.support.Metadata;
import com.helpshift.support.Support;
import com.helpshift.util.HelpshiftContext;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivatedCard_TutorialActivity;
import com.mobile.activities.LogInActivity;
import com.mobile.helpshift.HelpshiftIdentitfyVerificationHelper;
import com.mobile.history.PastReservationsFragment;
import com.mobile.loyalty.LoyaltyProgramFragment;
import com.mobile.model.Reservation;
import com.mobile.model.ScreeningToken;
import com.mobile.network.RestClient;
import com.mobile.reservation.ReservationActivity;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.text.TextUtils.isEmpty;
import static com.mobile.UserPreferences.*;
import static com.mobile.model.AvailabilityKt.fromTime;
import static java.lang.String.valueOf;

/**
 * Created by anubis on 5/31/17.
 */

public class ProfileFragment extends MPFragment {

    PastReservationsFragment pastReservations = new PastReservationsFragment();
    ReferAFriend refer = new ReferAFriend();
    View root;
    RelativeLayout details, history, currentRes, howToUse, help, referAFriend, loyaltyPrograms;
    TextView version, TOS, PP, signout;
    Switch pushSwitch;
    boolean pushValue;
    Context myContext;
    Realm historyRealm;
    Activity activity;

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
        activity = getActivity();


        RealmConfiguration historyConfig = new RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        historyRealm = Realm.getInstance(historyConfig);
        return root;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String versionName = BuildConfig.VERSION_NAME;
        version.setText("App Version: " + versionName);

        final String ppURL = "https://www.moviepass.com/privacy/";
        final String tosURL = "https://www.moviepass.com/terms";

        if (INSTANCE.getPushPermission()) {
            pushSwitch.setChecked(true);
        } else {
            pushSwitch.setChecked(false);
        }


        pushSwitch.setOnClickListener(v -> {
            if (pushSwitch.isChecked()) {
                INSTANCE.setPushPermission(true);
                pushValue = true;
            } else {
                INSTANCE.setPushPermission(false);
                pushValue = false;
            }

            try {
                JSONObject attributes = new JSONObject();
                attributes.put("pushPermission", pushValue);
                Taplytics.setUserAttributes(attributes);
                HelpshiftContext.getCoreApi().login(HelpshiftIdentitfyVerificationHelper.Companion.getHelpshiftUser());
            } catch (JSONException e) {

            }

        });


        signout.setOnClickListener(view16 -> {
            INSTANCE.clearUserId();
            INSTANCE.clearFbToken();
            historyRealm.executeTransactionAsync(realm -> realm.deleteAll());
//            UserPreferences.clearEverything();
            HelpshiftContext.getCoreApi().logout();
            Intent intent = new Intent(myContext, LogInActivity.class);
            startActivity(intent);
            activity.finishAffinity();
        });


        details.setOnClickListener(view1 -> {
            if (isOnline())
                showFragment(new ProfileAccountInformationFragment());
        });


        referAFriend.setOnClickListener(v -> {
            if (isOnline())
                showFragment(refer);
        });


        TOS.setOnClickListener(view13 -> {
            if (isOnline()) {
                Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tosURL));
                myContext.startActivity(notifIntent);
            }

        });

        Log.d(Constants.TAG, "onViewCreated: " + isOnline());
        PP.setOnClickListener(view14 -> {
            if (isOnline()) {
                Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ppURL));
                myContext.startActivity(notifIntent);
            }

        });

        help.setOnClickListener(view12 -> {
            if (isOnline()) {
                Map<String, String[]> customIssueFileds = new HashMap<>();
                HashMap<String, Object> userData = new HashMap<>();
                customIssueFileds.put("version name", new String[]{"sl", versionName});
                Long dateMillis = INSTANCE.getLastCheckInAttemptDate();
                if (dateMillis != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateMillis);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                    long diff = System.currentTimeMillis() - dateMillis;
                    long diffHours = diff / (60 * 60 * 1000);
                    long diffMinutes = diff / (60 * 1000);


                    customIssueFileds.put("last_check_in_attempt_date", new String[]{"sl", dateFormat.format(cal.getTime())});
                    customIssueFileds.put("last_check_in_attempt_time", new String[]{"sl", timeFormat.format(cal.getTime())});
                    customIssueFileds.put("hours_since_last_checkin_attempt", new String[]{"n", valueOf(diffHours)});
                    customIssueFileds.put("minutes_since_last_checkin_attempt", new String[]{"n", valueOf(diffMinutes)});
                }

                ScreeningToken token = INSTANCE.getLastReservation();
                final boolean checkedIn;

                if (token != null) {
                    Reservation rs = token.getReservation();
                    checkedIn = rs != null && rs.getExpiration() > System.currentTimeMillis();
                    Date starttime = token.getTimeAsDate();
                    if (starttime != null) {
                        long diff = starttime.getTime() - System.currentTimeMillis();
                        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);
                        if (checkedIn && minutes >= -30) {
                            customIssueFileds.put("minutes_until_showtime", new String[]{"n", valueOf(minutes)});
                        }
                    }
                } else {
                    checkedIn = false;
                }

                customIssueFileds.put("subscription_type", new String[]{"dd", INSTANCE.getRestrictions().getSubscriptionStatus().name()});
                customIssueFileds.put("checked_in", new String[]{"b", valueOf(checkedIn)});
                customIssueFileds.put("total_movies_seen", new String[]{"n", valueOf(INSTANCE.getTotalMovieSeen())});
                customIssueFileds.put("total_movies_seen_last_thirty_days", new String[]{"n", valueOf(INSTANCE.getTotalMovieSeenLastMonth())});
                userData.put("last_movie_seen", INSTANCE.getLastMovieSeen());
                String[] tags = new String[]{versionName};

                userData.put("version", versionName);

                Metadata meta = new Metadata(userData, tags);

                ApiConfig apiConfig = new ApiConfig.Builder()
                        .setEnableContactUs(Support.EnableContactUs.ALWAYS)
                        .setCustomIssueFields(customIssueFileds)
                        .setCustomMetadata(meta)
                        .build();

                if (activity != null) {
                    Support.showFAQs(activity, apiConfig);
                }
            }

        });


        history.setOnClickListener(view2 -> {
            showFragment(pastReservations);
        });

        currentRes.setOnClickListener(view1 -> {
            if (isOnline()) {
                RestClient
                        .getAuthenticated()
                        .lastReservation()
                        .subscribe(v -> {
                            if (myContext == null) {
                                return;
                            }
                            startActivity(ReservationActivity.Companion.newInstance(myContext, v, true));
                        }, e -> {
                            Toast.makeText(myContext, "No current reservation at this time", Toast.LENGTH_SHORT).show();
                        });
            }


        });


        howToUse.setOnClickListener(view15 -> {
            Intent intent = new Intent(myContext, ActivatedCard_TutorialActivity.class);
            startActivity(intent);
        });


        loyaltyPrograms.setOnClickListener(view1 -> {
            if (isOnline())
                showFragment(LoyaltyProgramFragment.Companion.newInstance());
        });


    }

    boolean isOnline() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = null;
        if (connectivityManager != null) {
            nInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (nInfo != null && nInfo.isConnected())
            return true;

        Toast.makeText(myContext, R.string.activity_no_internet_toast_message, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

}