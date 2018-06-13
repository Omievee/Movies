package com.mobile.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Toast;

import com.helpshift.support.ApiConfig;
import com.helpshift.support.Metadata;
import com.helpshift.support.Support;
import com.helpshift.util.HelpshiftContext;
import com.mobile.BackFragment;
import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.activities.ActivatedCard_TutorialActivity;
import com.mobile.activities.ConfirmationActivity;
import com.mobile.activities.LogInActivity;
import com.mobile.helpshift.HelpshiftIdentitfyVerificationHelper;
import com.mobile.model.Reservation;
import com.mobile.loyalty.LoyaltyProgramFragment;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.network.RestClient;
import com.mobile.reservation.ETicket;
import com.mobile.reservation.ReservationActivity;
import com.mobile.responses.ETicketConfirmation;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

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
    FragmentActivity myActivity;
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
                HelpshiftContext.getCoreApi().login(HelpshiftIdentitfyVerificationHelper.Companion.getHelpshiftUser());
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
            showFragment(new ProfileAccountInformationFragment());
        });

        referAFriend.setOnClickListener(v -> showFragment(refer));

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
            HashMap<String, Object> userData = new HashMap<>();
            customIssueFileds.put("version name", new String[]{"sl", versionName});
            Long dateMillis = UserPreferences.getLastCheckInAttemptDate();
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

            ScreeningToken token = UserPreferences.getLastReservation();
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

            customIssueFileds.put("subscription_type", new String[]{"dd", UserPreferences.getRestrictionSubscriptionStatus()});
            customIssueFileds.put("checked_in", new String[]{"b", valueOf(checkedIn)});
            customIssueFileds.put("total_movies_seen", new String[]{"n", valueOf(UserPreferences.getTotalMovieSeen())});
            customIssueFileds.put("total_movies_seen_last_thirty_days", new String[]{"n", valueOf(UserPreferences.getTotalMovieSeenLastMonth())});
            userData.put("last_movie_seen", UserPreferences.getLastMovieSeen());
            String[] tags = new String[]{versionName};

            userData.put("version", versionName);

            Metadata meta = new Metadata(userData, tags);

            ApiConfig apiConfig = new ApiConfig.Builder()
                    .setEnableContactUs(Support.EnableContactUs.ALWAYS)
                    .setCustomIssueFields(customIssueFileds)
                    .setCustomMetadata(meta)
                    .build();

            Support.showFAQs(myActivity, apiConfig);
        });

        history.setOnClickListener(view2 -> {
            showFragment(pastReservations);
        });

        currentRes.setOnClickListener(view1 -> {
            Activity activity = getActivity();
            RestClient
                    .getAuthenticated()
                    .lastReservation()
                    .subscribe(v -> {
                        if (activity == null) {
                            return;
                        }
                        final Intent intent;
                        ETicket ticket = v.getTicket();
                        if (ticket != null && !isEmpty(ticket.getRedemptionCode())) {
                            intent =
                                    ReservationActivity.Companion.newInstance(getActivity(), v);
                        } else {
                            Screening screening = Screening.Companion.from(v);
                            ETicketConfirmation confirmation = null;
                            if (v.getTicket() != null) {
                                confirmation = new ETicketConfirmation();
                                confirmation.setConfirmationCode(v.getTicket().getRedemptionCode());
                                confirmation.setBarCodeUrl("");
                            }
                            Reservation reservation = null;
                            if (v.getReservation() != null) {
                                reservation = new Reservation();
                                reservation.setId(v.getReservation().getId());
                            }
                            ScreeningToken token = new ScreeningToken(
                                    screening,
                                    new SimpleDateFormat("h:mm a").format(v.getShowtime()),
                                    reservation,
                                    confirmation,
                                    null
                            );
                            intent = new Intent(activity, ConfirmationActivity.class).putExtra(Constants.TOKEN, Parcels.wrap(token));
                        }
                        startActivity(intent);
                    }, e -> {
                        Toast.makeText(activity, "No current reservation at this time", Toast.LENGTH_SHORT).show();
                        //Snackbar.make(t)
                    });

        });

        howToUse.setOnClickListener(view15 -> {
            Intent intent = new Intent(myActivity, ActivatedCard_TutorialActivity.class);
            startActivity(intent);
        });

        loyaltyPrograms.setOnClickListener(view1 -> {
            showFragment(LoyaltyProgramFragment.Companion.newInstance());
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = getActivity();
    }

}