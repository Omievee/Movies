package com.moviepass.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.UserPreferences;
import com.moviepass.fragments.BrowseFragment;
import com.moviepass.fragments.ETicketFragment;
import com.moviepass.fragments.BrowseFragment;
import com.moviepass.fragments.MoviesFragment;
import com.moviepass.fragments.NotificationFragment;
import com.moviepass.fragments.ProfileFragment;
import com.moviepass.fragments.SettingsFragment;
import com.moviepass.fragments.TheatersFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.Movie;
import com.moviepass.network.RestClient;
import com.moviepass.responses.RestrictionsResponse;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    /* Permissions */
    public final static int REQUEST_LOCATION_CODE = 1000;
    public final static int REQUEST_STORAGE_CODE = 1001;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1004;

    private static String LOCATION_PERMISSIONS[] = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static String STORAGE_PERMISSIONS[] = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /* Location
    protected LocationUpdateBroadCast mLocationBroadCast;
    protected Location mLocation;
    protected boolean mDoUpdateLocation = true; */

    protected BottomNavigationView bottomNavigationView;

    AlertDialog alert;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "myprefs";

    /* Creation */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkRestrictions();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        updateNavigationBarState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkRestrictions();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void checkRestrictions() {
        RestClient.getAuthenticated().getRestrictions().enqueue(new Callback<RestrictionsResponse>() {
            @Override
            public void onResponse(Call<RestrictionsResponse> call, Response<RestrictionsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    RestrictionsResponse restriction = response.body();

                    String status = restriction.getSubscriptionStatus();
                    boolean threeDEnabled = restriction.get3dEnabled();
                    boolean allFormatsEnabled = restriction.getAllFormatsEnabled();
                    boolean verificationRequired = restriction.getProofOfPurchaseRequired();
                    boolean hasActiveCard = restriction.getHasActiveCard();

                    /* TODO : Update only if change */

                    UserPreferences.setRestrictions(status, threeDEnabled, allFormatsEnabled, verificationRequired, hasActiveCard);

                    //IF popInfo NOT NULL THEN INFLATE TicketVerificationActivity
                    if (restriction.getPopInfo() != null) {
                        Log.d("popInfo", restriction.getPopInfo().toString());

                        int reservationId = restriction.getPopInfo().getReservationId();
                        String movieTitle = restriction.getPopInfo().getMovieTitle();
                        String tribuneMovieId = restriction.getPopInfo().getTribuneMovieId();
                        String theaterName = restriction.getPopInfo().getTheaterName();
                        String tribuneTheaterId = restriction.getPopInfo().getTribuneTheaterId();
                        String showtime = restriction.getPopInfo().getShowtime();

                        Intent intent = new Intent(BaseActivity.this, VerificationActivity.class);
                        intent.putExtra("reservationId", reservationId);
                        intent.putExtra("movieTitle", movieTitle);
                        intent.putExtra("tribuneMovieId", tribuneMovieId);
                        intent.putExtra("theaterName", theaterName);
                        intent.putExtra("tribuneTheaterId", tribuneTheaterId);
                        intent.putExtra("showtime", showtime);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.d("jObjError", "jObjError: " + jObjError.getString("message"));

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
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alert = builder.create();
        alert.show();
    }
    */

}
