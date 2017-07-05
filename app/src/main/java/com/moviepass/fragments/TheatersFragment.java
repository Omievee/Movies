package com.moviepass.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.lapism.searchview.SearchView;
import com.moviepass.R;
import com.moviepass.TheatersClickListener;
import com.moviepass.activities.TheaterActivity;
import com.moviepass.adapters.TheatersAdapter;
import com.moviepass.model.Theater;
import com.moviepass.model.TheaterPin;
import com.moviepass.model.TheatersResponse;
import com.moviepass.network.RestClient;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/6/17.
 */

public class TheatersFragment extends Fragment implements OnMapReadyCallback, TheatersClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private final static int REQUEST_LOCATION_CODE = 0;
    final static byte DEFAULT_ZOOM_LEVEL = 12;
    int SUPPORT_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private HashMap<LatLng, Theater> mMapData;

    private GoogleApiClient mGoogleApiClient;
    boolean mLocationAcquired;
    private Location mMyLocation;
    private TheatersAdapter mTheatersAdapter;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private Boolean mRequestingLocationUpdates;

    GoogleMap mMap;
    MapView mMapView;

    private OnFragmentInteractionListener listener;
    SupportPlaceAutocompleteFragment places;

    SearchView mSearchLocation;
    ImageView mSearchClose;
    CardView mCardView;

    ArrayList<Theater> mTheaters;
    private ClusterManager<TheaterPin> mClusterManager;

    TheatersResponse mTheatersResponse;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    String TAG = "TAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_theaters, container, false);
        ButterKnife.bind(this, rootView);

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mSearchClose = rootView.findViewById(R.id.search_inactive);
        mSearchLocation = rootView.findViewById(R.id.search);
        mCardView = rootView.findViewById(R.id.card_view);
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(this);

        mMapData = new HashMap<>();

        mRequestingLocationUpdates = true;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        /* mLocationBroadCast = new LocationUpdateBroadCast();
        getActivity().registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER)); */

        /* Set up RecyclerView */
        mTheaters = new ArrayList<>();

        LinearLayoutManager mLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        mRecyclerView.setItemAnimator(itemAnimator);

        mTheatersAdapter = new TheatersAdapter(mTheaters, this);

        mSearchLocation.setVisibility(View.GONE);
        mSearchLocation.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onClose() {
                collapse(mSearchLocation);
                mSearchLocation.close(true);
                mSearchLocation.setVisibility(View.GONE);
                expand(mSearchClose);
                mSearchClose.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onOpen() {
                mSearchLocation.open(true);
                mSearchLocation.setVisibility(View.VISIBLE);
                mSearchClose.setVisibility(View.GONE);
                return false;
            }
        });

        mSearchClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchLocation.open(true);
                expand(mCardView);
                collapse(mSearchClose);
                mSearchLocation.setVisibility(View.VISIBLE);
                mSearchClose.setVisibility(View.GONE);
            }
        });

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .setCountry("US")
                .build();

        places = (SupportPlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setFilter(typeFilter);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mRequestingLocationUpdates = false;
                CameraUpdate current = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM_LEVEL);

                mMap.moveCamera(current);
                loadTheaters(place.getLatLng().latitude, place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getActivity().getApplicationContext(), status.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        places.setHint(getResources().getString(R.string.fragment_theaters_search));

        //Hide Keyboard when not in use
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Check if we were successful in obtaining the map.

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.map_style_json));
            mMap.getUiSettings().setMapToolbarEnabled(false);

            /* buildGoogleApiClient(); */

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateLocationUI();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH24:mm:ss");

            try {
                Date now = new Date();
                Date lastUpdate = simpleDateFormat.parse(mLastUpdateTime);

                long difference = now.getTime() - lastUpdate.getTime();
                if (difference > (10 * 60 * 1000) && mRequestingLocationUpdates) {
                    loadTheaters(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                    LatLng coordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);

                    mMap.moveCamera(current);

                    mClusterManager.cluster();
                }
            } catch (Exception e) {
                Log.d("exception", e.toString());
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateLocationUI();
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    getActivity().getWindow().getDecorView().getRootView(),
                    /* TODO */
                    "give permission to access your location",
                    Snackbar.LENGTH_INDEFINITE)
                    /* TODO */
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateLocationUI();
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
                Toast.makeText(getActivity(), "You must grant permission to use MoviePass.", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                /* TODO */
                /*
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });*/
            }
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void loadTheaters(Double latitude, final Double longitude) {
        mMap.clear();

        RestClient.getAuthenticated().getTheaters(latitude, longitude)
                .enqueue(new Callback<TheatersResponse>() {
                    @Override
                    public void onResponse(Call<TheatersResponse> call, final Response<TheatersResponse> response) {
                        TheatersResponse theaters = response.body();
                        if (theaters != null) {
                            List<Theater> theaterList = theaters.getTheaters();

                            if (theaterList.size() == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);

/*                            builder.setTitle(R.string.activity_location_no_theaters_found);
                            builder.setMessage(R.string.activity_location_try_another_zip_code);
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }); */

                                builder.show();
                            } else {
                                for (Theater theater : theaterList) {
                                    LatLng location = new LatLng(theater.getLat(), theater.getLon());

                                    mMapData.put(location, theater);

                                    //Initial View to Display RecyclerView Based on User's Current Location
                                    mTheatersResponse = response.body();
                                    mTheaters.clear();

                                    if (mTheatersAdapter != null) {
                                        mRecyclerView.getRecycledViewPool().clear();
                                        mTheatersAdapter.notifyDataSetChanged();
                                    }

                                    if (mTheatersResponse != null) {
                                        mTheaters.addAll(mTheatersResponse.getTheaters());
                                        mRecyclerView.setAdapter(mTheatersAdapter);
                                        mRecyclerView.setTranslationY(0);
                                        mRecyclerView.setAlpha(1.0f);
                                    }

                                    int position;
                                    position = theaterList.indexOf(theater);

                                    mClusterManager = new ClusterManager<>(getActivity(), mMap);
                                    mClusterManager.setRenderer(new TheaterPinRenderer());

                                    if (position == 0) {
                                        mClusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(),
                                                theater.getName(), R.drawable.theater_pin, position));
                                    } else {
                                        mClusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(),
                                                theater.getName(), R.drawable.theater_pin_unselected, position));
                                    }

                                    mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
                                    final CameraPosition[] mPreviousCameraPosition = {null};
                                    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                        @Override
                                        public void onCameraIdle() {
                                            CameraPosition position = mMap.getCameraPosition();
                                            if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                                                mPreviousCameraPosition[0] = mMap.getCameraPosition();
                                                mClusterManager.cluster();
                                            }
                                        }
                                    });

                                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                        @Override
                                        public void onInfoWindowClick(Marker marker) {
                                            mRequestingLocationUpdates = false;

                                            // Does nothing, but you could go into the user's profile page, for example.
                                            final Marker finalMarker = marker;


                                            mRecyclerView.animate()
                                                    .translationY(mRecyclerView.getHeight())
                                                    .alpha(0.5f)
                                                    .setListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            super.onAnimationEnd(animation);

                                                            //Load New RecyclerView Based on User's Click
                                                            loadTheaters(finalMarker.getPosition().latitude, finalMarker.getPosition().longitude);
                                                        }
                                                    });
                                        }
                                    });

                                    mClusterManager.cluster();

                                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                        @Override
                                        public void onMapClick(LatLng arg0) {
                                            if (mCardView.getVisibility() == View.VISIBLE) {
                                                collapse(mCardView);
                                            }

                                            if (mSearchClose.getVisibility() == View.GONE) {
                                                expand(mSearchClose);
                                            }

                                            if (mRecyclerView != null) {
                                                mRecyclerView.animate()
                                                        .translationY(mRecyclerView.getHeight())
                                                        .alpha(0.5f)
                                                        .setListener(new AnimatorListenerAdapter() {
                                                            @Override
                                                            public void onAnimationEnd(Animator animation) {
                                                                super.onAnimationEnd(animation);

                                                                mTheatersAdapter.notifyDataSetChanged();
                                                                mRecyclerView.getRecycledViewPool().clear();
                                                                mTheaters.clear();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TheatersResponse> call, Throwable t) {
                        if (t != null) {
                            Log.d("Unable to get theaters", "Unable to download theaters: " + t.getMessage());
                        }
                    }

                });
    }

    private List<Theater> filterStandardTheaters(List<Theater> allTheaters) {
        List<Theater> filteredTheaters = new ArrayList<>();

        if (allTheaters != null) {
            for (Theater theater : allTheaters) {

                if (!theater.getTicketType().matches("STANDARD")) {
                    filteredTheaters.add(theater);
                }
            }
        }

        return filteredTheaters;
    }

    private class TheaterPinRenderer extends DefaultClusterRenderer<TheaterPin> {

        public TheaterPinRenderer() {
            super(getActivity().getApplicationContext(), mMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(TheaterPin theaterPin, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.

            if (theaterPin.getArrayPosition() == 0) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin)).title(theaterPin.getTitle());
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin_unselected)).title(theaterPin.getTitle());
            }
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<TheaterPin> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> theaterPins = new ArrayList<>(Math.min(4, cluster.getSize()));
            //int width = mDimension;
            //int height = mDimension;

            for (TheaterPin p : cluster.getItems()) {
                // Draw 4 at most.
                if (theaterPins.size() == 4) break;
                Drawable drawable = getResources().getDrawable(R.drawable.theater_pin_unselected);
                //drawable.setBounds(0, 0, width, height);
                theaterPins.add(drawable);
            }
            /*MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)); */
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 2;
        }
    }

    public void onTheaterClick(int pos, Theater theater) {
        Intent intent = new Intent(getActivity(), TheaterActivity.class);
        intent.putExtra(TheaterActivity.THEATER, Parcels.wrap(theater));

        startActivity(intent);
    }

    public interface OnFragmentInteractionListener {
    }

    public static void expand(final View v) {
        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targetedHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RelativeLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetedHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int)(targetedHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}