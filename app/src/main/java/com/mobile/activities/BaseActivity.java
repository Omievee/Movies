package com.mobile.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.helpshift.util.HelpshiftContext;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.fragments.NoInternetFragment;
import com.mobile.network.RestClient;
import com.mobile.responses.RestrictionsResponse;
import com.mobile.responses.UserInfoResponse;
import com.taplytics.sdk.Taplytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import com.taplytics.sdk.Taplytics;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    int offset = 3232323;

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
    protected BottomNavigationView bottomNavigationView;

    public String myZip;

    AlertDialog alert;
    public static final String MyPREFERENCES = "myprefs";

    /* Creation */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("email", UserPreferences.getUserEmail());
            attributes.put("name", UserPreferences.getUserName());
            attributes.put("user_id", String.valueOf(UserPreferences.getUserId()));
            Log.d("taplytics put", UserPreferences.getUserEmail());
            Taplytics.setUserAttributes(attributes);
        } catch (JSONException e) {

        }

        try {
            HelpshiftContext.getCoreApi().login(String.valueOf(UserPreferences.getUserId()), UserPreferences.getUserName(), UserPreferences.getUserEmail());
        } catch (Exception e) {

        }

        checkRestrictions();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRestrictions();
        if (!isOnline()) {
            NoInternetFragment fragobj = new NoInternetFragment();
            FragmentManager fm = getSupportFragmentManager();
            fragobj.show(fm, "fr_no_internet");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public void checkRestrictions() {
        RestClient.getAuthenticated().getRestrictions(UserPreferences.getUserId() + offset).enqueue(new Callback<RestrictionsResponse>() {
            @Override
            public void onResponse(Call<RestrictionsResponse> call, Response<RestrictionsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    restriction = response.body();
                    String status = restriction.getSubscriptionStatus();
                    boolean fbPresent = restriction.getFacebookPresent();
                    boolean threeDEnabled = restriction.get3dEnabled();
                    boolean allFormatsEnabled = restriction.getAllFormatsEnabled();
                    boolean verificationRequired = restriction.getProofOfPurchaseRequired();
                    boolean hasActiveCard = restriction.getHasActiveCard();
                    boolean subscriptionActivationRequired = restriction.isSubscriptionActivationRequired();

                    if (!UserPreferences.getRestrictionSubscriptionStatus().equals(status) ||
                            UserPreferences.getRestrictionFacebookPresent() != fbPresent ||
                            UserPreferences.getRestrictionThreeDEnabled() != threeDEnabled ||
                            UserPreferences.getRestrictionAllFormatsEnabled() != allFormatsEnabled ||
                            UserPreferences.getRestrictionVerificationRequired() != verificationRequired ||
                            UserPreferences.getRestrictionHasActiveCard() != hasActiveCard ||
                            UserPreferences.getIsSubscriptionActivationRequired() != subscriptionActivationRequired) {

                        UserPreferences.setRestrictions(status, fbPresent, threeDEnabled, allFormatsEnabled, verificationRequired, hasActiveCard, subscriptionActivationRequired);
                    }

                    //IF popInfo NOT NULL THEN INFLATE TicketVerificationActivity
                    if (restriction.getPopInfo() != null) {

                        int reservationId = restriction.getPopInfo().getReservationId();
                        String movieTitle = restriction.getPopInfo().getMovieTitle();
                        String tribuneMovieId = restriction.getPopInfo().getTribuneMovieId();
                        String theaterName = restriction.getPopInfo().getTheaterName();
                        String tribuneTheaterId = restriction.getPopInfo().getTribuneTheaterId();
                        String showtime = restriction.getPopInfo().getShowtime();

                        Intent intent = new Intent(BaseActivity.this, VerificationActivity.class);
                        intent.putExtra("reservationId", reservationId);
                        intent.putExtra("mSelectedMovieTitle", movieTitle);
                        intent.putExtra("tribuneMovieId", tribuneMovieId);
                        intent.putExtra("mTheaterSelected", theaterName);
                        intent.putExtra("tribuneTheaterId", tribuneTheaterId);
                        intent.putExtra("showtime", showtime);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        //IF API ERROR LOG OUT TO LOG BACK IN
                        /*
                        if (jObjError.getString("message").matches("INVALID API REQUEST")) {

                            UserPreferences.resetUserCredentials();
                            Intent intent = new Intent(BaseActivity.this, LauncherActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            finish();
                        }

                        */

                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<RestrictionsResponse> call, Throwable t) {

            }
        });
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

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

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


    /*

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    Toast.makeText(BaseActivity.this, "Profile Activity", Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.action_reservations) {
                    Toast.makeText(BaseActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), ETicketsActivity.class));
                } else if (itemId == R.id.action_browse) {
                    startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
                } else if (itemId == R.id.action_notifications) {
                    Toast.makeText(BaseActivity.this, "Notification Activity", Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.action_settings) {
                    Toast.makeText(BaseActivity.this, "Settings Activity", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState(){
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

    abstract int getContentViewId();

    abstract int getNavigationMenuItemId();




    /* Handle Permissions
    public void requestMandatoryPermissions(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(BaseActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_CODE);
            }
            if (ContextCompat.checkSelfPermission(BaseActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(STORAGE_PERMISSIONS, REQUEST_STORAGE_CODE);
            }
        }
    }

    public boolean grantedMandatoryPermissions(){
        boolean locationPermissionResult = ContextCompat.checkSelfPermission(BaseActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean storagePermissionResult = ContextCompat.checkSelfPermission(BaseActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return locationPermissionResult && storagePermissionResult;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // Check for ACCESS_FINE_LOCATION
                if (grantedMandatoryPermissions()) {
                    // All Permissions Granted
                    locationInit();
                } else {
                    // Permission Denied
                    Toast.makeText(BaseActivity.this, R.string.activity_main_mandatory_permissions,
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /* Handle Location
    class LocationUpdateBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (grantedMandatoryPermissions()) {
                if (!isLocationUserDefined()) {
                    onLocationChanged(UserLocationManagerFused.getLocationInstance(context).mCurrentLocation);
                }
            } else {
                requestMandatoryPermissions();
            }
        }
    }

    protected void onLocationChanged(Location location) {
        if (grantedMandatoryPermissions()) {
            if (!UserPreferences.getIsLocationUserDefined() && mDoUpdateLocation && location != null) {
                try {
                    mLocation = location;

                    UserPreferences.setCoordinates(location.getLatitude(), location.getLatitude());
                } catch (Exception e) {
                    FirebaseCrash.report(new Exception(e.getMessage()));
                }
            }

            /* TODO : INITIAL ACTIVITY

            if (mIsIntialActivity) {
                UserLocationManagerFused.getLocationInstance(this).stopLocationUpdates();
            }


        } else {
            requestMandatoryPermissions();
        }
    }

    public void locationInit() {
        if (grantedMandatoryPermissions()) {
            LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!enabled) {
                showDialogGPS();
            }

            if (!UserPreferences.getIsLocationUserDefined() && mDoUpdateLocation) {
                mLocationBroadCast = new LocationUpdateBroadCast();
                UserLocationManagerFused.getLocationInstance(this).startLocationUpdates();
            }

            /* TODO
            if (isOnline()) {
            } else {
                noInternetCheckLoop();
            }


        } else {
            requestMandatoryPermissions();
        }
    }

    public static boolean isLocationUserDefined() {
        return UserPreferences.getIsLocationUserDefined();
    }

    /* TODO SET FLOW FOR USERS WHO DENY LOCATION PERMISSION

    private void showDialogGPS() {
        if (alert != null && alert.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this, R.style.AlertDialogCustom);
        builder.setCancelable(false);
        builder.setTitle("Enable GPS");
        builder.setMessage("You must enable your GPS to use MoviePass.");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(
                        new Intent(android.Provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alert = builder.create();
        alert.show();
    }
    */


