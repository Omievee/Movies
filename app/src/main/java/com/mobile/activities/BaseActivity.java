package com.mobile.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.helpshift.util.HelpshiftContext;

import com.mobile.helpers.LogUtils;
import com.mobile.helpshift.HelpshiftIdentitfyVerificationHelper;
import com.mobile.responses.RestrictionsResponse;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mobile.UserPreferences.getUserEmail;
import static com.mobile.UserPreferences.getUserId;
import static com.mobile.UserPreferences.getUserName;
import static java.lang.String.valueOf;


public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    int offset = 3232323;
    Bundle bundle;
    /* Permissions */
    public final static int REQUEST_LOCATION_CODE = 1000;
    public final static int REQUEST_STORAGE_CODE = 1001;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1004;
    public RestrictionsResponse restriction;
    private static String LOCATION_PERMISSIONS[] = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static String STORAGE_PERMISSIONS[] = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    UserInfoResponse userInfoResponse;
    public BottomNavigationView bottomNavigationView;

    public String myZip;

    AlertDialog alert;
    public static final String MyPREFERENCES = "myprefs";

    /* Creation */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("email", getUserEmail());
            attributes.put("name", getUserName());
            attributes.put("user_id", valueOf(getUserId()));
            LogUtils.newLog("taplytics put", getUserEmail());
            Taplytics.setUserAttributes(attributes);
        } catch (JSONException e) {

        }

        try {
            HelpshiftContext.getCoreApi().login(HelpshiftIdentitfyVerificationHelper.Companion.getHelpshiftUser());
        } catch (Exception e) {

        }


    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //COMMENTED OUT - ALEXIS WANTED A TOAST INSTEAD

        checkInternetConnection();
    }

    public void checkInternetConnection() {
        if (!isOnline()) {
            Toast.makeText(this, getResources().getString(R.string.activity_no_internet_toast_message), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    public boolean isOnline() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo nInfo = connectivityManager.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(500);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);
    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }


    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        Animation animate = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }


        };

        animate.setDuration((long) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(animate);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((long) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


}


