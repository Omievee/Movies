package com.moviepass.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
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
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.lapism.searchview.SearchView;
import com.moviepass.R;
import com.moviepass.adapters.TheatersAdapter;
import com.moviepass.fragments.TheatersFragment;
import com.moviepass.listeners.TheatersClickListener;
import com.moviepass.model.Theater;
import com.moviepass.model.TheaterPin;
import com.moviepass.model.TheatersResponse;
import com.moviepass.network.RestClient;

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
 * Created by anubis on 8/22/17.
 */

public class ViewTheatersActivity extends AppCompatActivity implements OnMapReadyCallback, TheatersClickListener,
        GoogleApiClient.OnConnectionFailedListener, ClusterManager.OnClusterClickListener<TheaterPin>  {

    public static final String EXTRA_CIRCULAR_REVEAL_TRANSITION_NAME = "circular_reveal_transition_name";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    final static byte DEFAULT_ZOOM_LEVEL = 10;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private HashMap<LatLng, Theater> mMapData;

    private GoogleApiClient mGoogleApiClient;
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

    private TheatersFragment.OnFragmentInteractionListener listener;
    SupportPlaceAutocompleteFragment places;

    SearchView mSearchLocation;
    ImageView mSearchClose;
    CardView mCardView;
    View mProgress;
    RelativeLayout mRelativeLayout;

    ArrayList<Theater> mTheaters;
    private ClusterManager<TheaterPin> mClusterManager;

    boolean isRecyclerViewShown;

    TheatersResponse mTheatersResponse;
    TheatersFragment.OnTheaterSelect theaterSelect;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    String TAG = "TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_theaters);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mRelativeLayout = findViewById(R.id.relative_layout);
        mSearchClose = findViewById(R.id.search_inactive);
        mSearchLocation = findViewById(R.id.search);
        mCardView = findViewById(R.id.card_view);
        mProgress = findViewById(R.id.progress);
        mMapView = findViewById(R.id.mapView);

        //mMapView.onResume();// needed to get the map to display immediately

        mRequestingLocationUpdates = true;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mMapData = new HashMap<>();

        /* Set up RecyclerView */
        mTheaters = new ArrayList<>();

        LinearLayoutManager mLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(250);
        itemAnimator.setRemoveDuration(250);
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
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setFilter(typeFilter);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mRequestingLocationUpdates = false;

                loadTheaters(place.getLatLng().latitude, place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), status.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        places.setHint(getResources().getString(R.string.fragment_theaters_search));

        //Hide Keyboard when not in use
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        updateLocationUI();
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
        mMap.setMinZoomPreference(10);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json));
            mMap.getUiSettings().setMapToolbarEnabled(false);

            mClusterManager = new ClusterManager<>(this, mMap);
            mClusterManager.setRenderer(new ViewTheatersActivity.TheaterPinRenderer());
            mMap.setOnMarkerClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(this);

            if (mRequestingLocationUpdates && checkPermissions()) {
                startLocationUpdates();
            } else if (!checkPermissions()) {
                requestPermissions();
            }

            updateLocationUI();

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }

            mClusterManager.cluster();
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();

        if (mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

        updateLocationUI();

        if (mRecyclerView.getVisibility() != View.VISIBLE && !mRequestingLocationUpdates) {
            loadTheaters(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRecyclerView.setVisibility(View.INVISIBLE);
        mClusterManager.clearItems();
        mClusterManager.cluster();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        if (mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
    }

    /*
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        theaterSelect = (TheatersFragment.OnTheaterSelect) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClusterManager.clearItems();
        mClusterManager.cluster();
        listener = null;
    }
    */

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
            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH24:mm:ss");

                try {
                    Date now = new Date();
                    Date lastUpdate = simpleDateFormat.parse(mLastUpdateTime);

                    long difference = now.getTime() - lastUpdate.getTime();

                    if (difference > (10 * 60 * 1000) && mRequestingLocationUpdates) {
                        Log.d("difference", String.valueOf(difference));

                        if (mClusterManager != null) {
                            mClusterManager.clearItems();
                            mClusterManager.cluster();
                        }

                        loadTheaters(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    }
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            } else {
                if (mRequestingLocationUpdates) {
                    loadTheaters(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }
            }
        } else {
            if (checkPermissions()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location loc = task.getResult();

                        if (mRequestingLocationUpdates) {
                            loadTheaters(loc.getLatitude(), loc.getLongitude());

                            LatLng coordinates = new LatLng(loc.getLatitude(), loc.getLongitude());
                            CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);

                            mMap.moveCamera(current);
                        }
                    }
                });
            } else {
                requestPermissions();
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

        //noinspection MissingPermission
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful() && task.getResult() != null){
                    Log.d("MainActivity", "Result: " + task.getResult());
                    // Location mCurrentLocation = task.getResult();

                    updateLocationUI();
                }
            }
        });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    getWindow().getDecorView().getRootView(),
                    /* TODO */
                    "give permission to access your location",
                    Snackbar.LENGTH_INDEFINITE)
                    /* TODO */
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(ViewTheatersActivity.this,
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
            ActivityCompat.requestPermissions(this,
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
                Toast.makeText(this, "You must grant permission to use MoviePass.", Toast.LENGTH_SHORT).show();
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
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void loadTheaters(Double latitude, final Double longitude) {
        mMap.clear();
        mTheaters.clear();
        mProgress.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        LatLng latlng = new LatLng(latitude, longitude);
        CameraUpdate current = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM_LEVEL);
        Log.d("loadTheaters", latlng.toString());
        mMap.moveCamera(current);
        mMap.animateCamera(current);

        RestClient.getAuthenticated().getTheaters(latitude, longitude)
                .enqueue(new Callback<TheatersResponse>() {
                    @Override
                    public void onResponse(Call<TheatersResponse> call, final Response<TheatersResponse> response) {
                        TheatersResponse theaters = response.body();
                        if (theaters != null) {
                            List<Theater> theaterList = theaters.getTheaters();

                            if (theaterList.size() == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewTheatersActivity.this, R.style.AlertDialogCustom);

/*                    builder.setTitle(R.string.activity_location_no_theaters_found);
                    builder.setMessage(R.string.activity_location_try_another_zip_code);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }); */

                                builder.show();
                            } else {
                                mClusterManager.clearItems();
                                mClusterManager.cluster();

                                for (final Theater theater : theaterList) {
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
                                        isRecyclerViewShown = true;
                                        mProgress.setVisibility(View.GONE);
                                    }

                                    final int position;
                                    position = theaterList.indexOf(theater);


                                    mClusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(),
                                            theater.getName(), R.drawable.theater_pin, position, theater));

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

                                    mClusterManager.cluster();

                                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                        @Override
                                        public void onInfoWindowClick(final Marker marker) {
                                            mRequestingLocationUpdates = false;
                                            final Marker finalMarker = marker;
                                            mProgress.setVisibility(View.VISIBLE);

                                            mRecyclerView.animate()
                                                    .translationY(mRecyclerView.getHeight())
                                                    .alpha(0.5f)
                                                    .setListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            super.onAnimationEnd(animation);

                                                            //Load New RecyclerView Based on User's Click
                                                            mClusterManager.clearItems();
                                                            mClusterManager.cluster();

                                                            Projection projection = mMap.getProjection();
                                                            LatLng markerLocation = finalMarker.getPosition();
                                                            Point screenPosition = projection.toScreenLocation(markerLocation);

                                                            onTheaterClick(position, theater, screenPosition.x, screenPosition.y);
                                                        }
                                                    });
                                        }
                                    });

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
                                                if (isRecyclerViewShown) {
                                                    mRecyclerView.animate()
                                                            .translationY(mRecyclerView.getHeight())
                                                            .alpha(0.5f)
                                                            .setListener(new AnimatorListenerAdapter() {
                                                                @Override
                                                                public void onAnimationEnd(Animator animation) {
                                                                    super.onAnimationEnd(animation);

                                                                    isRecyclerViewShown = false;
                                                                }
                                                            });
                                                } else {
                                                    mRecyclerView.animate()
                                                            .translationY(0)
                                                            .alpha(1.0f)
                                                            .setListener(new AnimatorListenerAdapter() {
                                                                @Override
                                                                public void onAnimationEnd(Animator animation) {
                                                                    super.onAnimationEnd(animation);

                                                                    isRecyclerViewShown = true;
                                                                }
                                                            });
                                                }
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
        private final IconGenerator mClusterIconGenerator;

        public TheaterPinRenderer() {
            super(ViewTheatersActivity.this, mMap, mClusterManager);
            mClusterIconGenerator = new IconGenerator(ViewTheatersActivity.this);
        }

        @Override
        protected void onBeforeClusterItemRendered(TheaterPin theaterPin, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin)).title(theaterPin.getTitle());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<TheaterPin> cluster, MarkerOptions markerOptions) {
            try {
                mClusterIconGenerator.setBackground(
                        ContextCompat.getDrawable(ViewTheatersActivity.this, R.drawable.icon_clustered_theater_pin));

                mClusterIconGenerator.setTextAppearance(R.style.ThemeOverlay_AppCompat_Dark);

                final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

                // Draw multiple people.
                // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
                List<Drawable> theaterPins = new ArrayList<>(Math.min(4, cluster.getSize()));

                for (TheaterPin p : cluster.getItems()) {
                    // Draw 4 at most.
                    if (theaterPins.size() == 4) break;
                    Drawable drawable = getResources().getDrawable(R.drawable.theater_pin);
                    //drawable.setBounds(0, 0, width, height);
                    theaterPins.add(drawable);
                }
            } catch (Exception e) {
                Log.d("onBeforeClusterRender: ", e.toString());
            }
        }

        @Override
        protected void onClusterRendered(Cluster<TheaterPin> cluster, Marker marker) {
            super.onClusterRendered(cluster, marker);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 2;
        }
    }

    @Override
    public boolean onClusterClick(final Cluster<TheaterPin> cluster) {
        Log.d("onClusterClick", "clusterClick");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                cluster.getPosition(), (float) Math.floor(mMap
                        .getCameraPosition().zoom + 1)), 300,
                null);

        return true;
    }

    public void onTheaterClick(int pos, Theater theater, int cx, int cy) {
        collapse(mCardView);

        mClusterManager.clearItems();
        mClusterManager.cluster();

        int recyclerViewHeight = mRecyclerView.getHeight();
        float screenHeight = mRelativeLayout.getHeight();

        int finalCx = cx;
        int finalCy = (int) screenHeight + recyclerViewHeight - cy;

        onTheaterSelect(pos, theater, finalCx, finalCy);
    }

    public void onTheaterSelect(int pos, Theater theater, int cx, int cy) {
        theaterSelect.onTheaterSelect(pos, theater, cx, cy);
    }

    public interface OnTheaterSelect {
        public void onTheaterSelect(int pos, Theater theater, int cx, int cy);
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