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
import android.view.MenuItem;
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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener,
        ETicketFragment.OnFragmentInteractionListener, BrowseFragment.OnFragmentInteractionListener,
        NotificationFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        TheatersFragment.OnFragmentInteractionListener, MoviesFragment.OnFragmentInteractionListener {

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

    /* Location */
    protected LocationUpdateBroadCast mLocationBroadCast;
    protected Location mLocation;
    protected boolean mDoUpdateLocation = true;

    AlertDialog alert;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "myprefs";

    /* Creation */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Marshmallow+ Request Permissions */
        if (grantedMandatoryPermissions()) {

            /* First Run Actions -- set default values */
            if (String.valueOf(UserPreferences.getIsUserFirstLogin()).matches(Constants.IS_USER_FIRST_LOGIN)) {
                UserPreferences.setIsUserFirstLogin(false);
                UserPreferences.setIsLocationUserDefined(false);
            }

            locationInit();
        } else {
            requestMandatoryPermissions();
        }

        /* Set up the Toolbar */
        /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); */

        /* Create Bottom Navigation Menu */
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);


        /* Select View */
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_profile:
                                fragment = ProfileFragment.newInstance();
                                break;
                            case R.id.action_e_tickets:
                                fragment = ETicketFragment.newInstance();
                                break;
                            case R.id.action_browse:
                                if (grantedMandatoryPermissions()) {
                                    fragment = BrowseFragment.newInstance();
                                } else {
                                    requestMandatoryPermissions();
                                }
                                break;
                            case R.id.action_notifications:
                                fragment = NotificationFragment.newInstance();
                                break;
                            case R.id.action_settings:
                                fragment = SettingsFragment.newInstance();
                                break;
                        }

                        if (fragment != null) {
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.frame_layout, fragment);
                            fragmentTransaction.commit();
                        }

                        return true;
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (grantedMandatoryPermissions()) {
            if (!UserPreferences.getIsLocationUserDefined() && mLocationBroadCast != null) {
                registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
            }

            LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!enabled) {
                showDialogGPS();
            }
        } else {
            requestMandatoryPermissions();
        }

        /* TODO :

        if (!isOnline()) {
            noInternetCheckLoop();
        }

        */

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationBroadCast != null) {
            try {
                unregisterReceiver(mLocationBroadCast);
            } catch (Exception e) {
                Log.d("BaseActivity onPause", "unregister received" + e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationBroadCast != null) {
            try {
                unregisterReceiver(mLocationBroadCast);
            } catch (Exception e) {
                Log.d("BaseActivity onPause", "unregister received" + e.getMessage());
            }
        }
    }

    /*

    /* Handle Permissions */
    public void requestMandatoryPermissions(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_CODE);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(STORAGE_PERMISSIONS, REQUEST_STORAGE_CODE);
            }
        }
    }

    public boolean grantedMandatoryPermissions(){
        boolean locationPermissionResult = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean storagePermissionResult = ContextCompat.checkSelfPermission(MainActivity.this,
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
                    Toast.makeText(MainActivity.this, R.string.activity_main_mandatory_permissions,
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /* Handle Location */
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

            */
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

            */
        } else {
            requestMandatoryPermissions();
        }
    }

    public static boolean isLocationUserDefined() {
        return UserPreferences.getIsLocationUserDefined();
    }

    /* TODO SET FLOW FOR USERS WHO DENY LOCATION PERMISSION */

    private void showDialogGPS() {
        if (alert != null && alert.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogCustom);
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

}
