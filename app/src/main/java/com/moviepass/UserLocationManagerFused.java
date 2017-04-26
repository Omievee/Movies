package com.moviepass;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.moviepass.application.Application;
import com.moviepass.events.UIRefreshEvent;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class UserLocationManagerFused implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static final int MILLISECONDS_PER_SECOND = 1000, FASTEST_INTERVAL_IN_SECONDS = 1, DELAYED_INTERVAL_IN_SECONDS = 30;

    private static final long UPDATE_INTERVAL = 5000;
    private static final long DELAYED_UPDATE_INTERVAL = 20000;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
            * FASTEST_INTERVAL_IN_SECONDS;

    private static final long DELAYED_INTERVAL = MILLISECONDS_PER_SECOND
            * DELAYED_INTERVAL_IN_SECONDS;

    private Geocoder gcd;
    private static UserLocationManagerFused ourInstance;
    private Context context;

    public static UserLocationManagerFused getLocationInstance(Context context) {
        if (ourInstance != null) {
            ourInstance.context = context;
        } else {
            ourInstance = new UserLocationManagerFused(context);
        }
        return ourInstance;
    }

    public boolean isLocationEnabled() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }

    private UserLocationManagerFused(Context context) {
        this.context = context;
        gcd = new Geocoder(Application.getInstance(), Locale.getDefault());
        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        initLocClient();
    }

    private void initLocClient() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private boolean setLocationUpdateInterval() {
        if (mLocationRequest != null) {
            stopLocationUpdates();
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(DELAYED_UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(DELAYED_INTERVAL);
            mLocationRequest.setSmallestDisplacement(10.0f);
            startLocationUpdates();
            return true;
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mCurrentLocation = location;

            if (!UserPreferences.getIsLocationUserDefined()) {
                updateLocation(mCurrentLocation);
            }

            Intent i = new Intent(Constants.LOCATION_UPDATE_INTENT_FILTER);
            context.sendBroadcast(i);
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();

        if (mCurrentLocation != null) {
            updateLocation(mCurrentLocation);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }

    }

    public void stopLocationUpdates() {
        if(mGoogleApiClient!=null&&mGoogleApiClient.isConnected())
        {LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);}
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void updateLocation(Location loc) {
        String cityName = null;
        String stateName = null;
        String stateNameAbrev = null;
        String zipCode = null;
        List<Address> addresses;
        boolean isLocationUserDefined = false;

        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

            if (addresses.size() > 0) {

                Address address = addresses.get(0);
                cityName = address.getLocality();
                if (cityName == null) {
                    cityName = address.getSubLocality();
                }

                stateName = address.getAdminArea() == null ? "" : address.getAdminArea().toLowerCase();
                stateNameAbrev = Constants.STATES_AND_STATES_ABREV.get(stateName);
                zipCode = address.getPostalCode();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        UserPreferences.setLocation(formatCityAndState(cityName, stateNameAbrev), zipCode, loc.getLatitude(), loc.getLongitude(), isLocationUserDefined);
    }

    private String formatCityAndState(String cityName, String subCityName) {
        String formattedStr;

        if (subCityName != null) {
            formattedStr = cityName + ", " + subCityName;
        } else {
            formattedStr = cityName;
        }

        return formattedStr;
    }

    public void requestLocationForZipCode(final String zipCode, Context ctx) {

        if (zipCode.length() > 0) {

            final String addressToFind = zipCode + ", " + Constants.UNITED_STATES_PREFIX;
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    List<Address> addresses = null;

                    try {
                        addresses = gcd.getFromLocationName(addressToFind, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addresses != null && addresses.size() > 0) {
                        String cityName = null;
                        String stateName = null;
                        String stateNameAbrev = null;
                        boolean isLocationUserDefined = true;
                        double lat, lon;
                        Address address = addresses.get(0);

                        lat = address.getLatitude();
                        lon = address.getLongitude();
                        cityName = address.getLocality();
                        if (cityName == null) {
                            cityName = address.getSubLocality();
                        }
                        stateName = address.getAdminArea().toLowerCase();
                        stateNameAbrev = Constants.STATES_AND_STATES_ABREV.get(stateName);

                        UserPreferences.setLocation(formatCityAndState(cityName, stateNameAbrev), zipCode, lat, lon, isLocationUserDefined);

                        /* TODO : switch to rxJava */
                        EventBus.getDefault().post(new UIRefreshEvent<>(UIRefreshEvent.EventName.USER_ZIP_CODE_CHANGED, null));
                    }

                }
            };

            handler.postDelayed(runnable, 10);
        } else {

            if (ctx != null) {
                Toast.makeText(ctx, R.string.user_location_manager_fused_zip_code_empty, Toast.LENGTH_LONG).show();
            }

        }
    }

    public void requestLocationForCoords(final double lat, final double lng, Context ctx) {

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            double lat = UserPreferences.getLatitude();
            double lng = UserPreferences.getLongitude();

            @Override
            public void run() {
                List<Address> addresses = null;

                try {
                    addresses = gcd.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addresses != null && addresses.size() > 0) {
                    String cityName = null;
                    String stateName = null;
                    String stateNameAbrev = null;
                    double lat, lon;
                    String zip = null;
                    boolean isLocationUserDefined = true;
                    Address address = addresses.get(0);

                    lat = UserPreferences.getLatitude();
                    lon = UserPreferences.getLongitude();
                    cityName = address.getLocality();
                    if (cityName == null) {
                        cityName = address.getSubLocality();
                    }
                    zip = address.getPostalCode() != null?address.getPostalCode():"0";
                    stateName = address.getAdminArea()!=null?address.getAdminArea().toLowerCase():"";
                    stateNameAbrev = Constants.STATES_AND_STATES_ABREV.get(stateName);

                    UserPreferences.setLocation(formatCityAndState(cityName, stateNameAbrev), zip, lat, lon, isLocationUserDefined);

                    /* TODO : SWITCH TO rxJAVA */
                    EventBus.getDefault().post(new UIRefreshEvent<>(UIRefreshEvent.EventName.USER_LOCATION_CHANGED, null));
                }

            }
        };

        handler.postDelayed(runnable, 10);
    }

}
