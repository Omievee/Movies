package com.mobile.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
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
import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.UserPreferences;
import com.mobile.helpers.ContextSingleton;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.listeners.TheatersClickListener;
import com.mobile.adapters.TheatersAdapter;
import com.mobile.model.Theater;
import com.mobile.model.TheaterPin;
import com.mobile.model.TheatersResponse;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.responses.GoWatchItResponse;
import com.moviepass.BuildConfig;
import com.moviepass.R;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;


public class TheatersFragment extends Fragment implements OnMapReadyCallback, TheatersClickListener,
        GoogleApiClient.OnConnectionFailedListener, ClusterManager.OnClusterClickListener<TheaterPin>, LocationListener {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    final static byte DEFAULT_ZOOM_LEVEL = 10;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final int LOCATION_PERMISSIONS = 99;

    private HashMap<LatLng, Theater> mMapData;
    private HashMap<String, Theater> markerTheaterMap;

    private GoogleApiClient mGoogleApiClient;
    private TheatersAdapter theatersMapViewAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private Boolean mRequestingLocationUpdates;
    double lat, lon;
    LocationListener locaListener;
    LocationManager locManager;
    GoogleMap mMap;
    MapView mMapView;
    String url;

    private OnFragmentInteractionListener listener;

    ImageView mSearchClose, myloc, listSwitch;
    View mProgress;
    RelativeLayout mRelativeLayout;
    LatLng markerPosition;
    ArrayList<Theater> mTheaters;
    private ClusterManager<TheaterPin> mClusterManager;
    boolean isRecyclerViewShown;
    TheatersResponse mTheatersResponse;
    OnTheaterSelect theaterSelect;


    @BindView(R.id.recycler_view)
    RecyclerView theatersMapViewRecycler, theatersListRecyclerview;

    String TAG = "TAG";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_theaters, container, false);
        ButterKnife.bind(this, rootView);

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mRelativeLayout = rootView.findViewById(R.id.relative_layout);
        mSearchClose = rootView.findViewById(R.id.search_inactive);
        mProgress = rootView.findViewById(R.id.progress);
        mMapView = rootView.findViewById(R.id.mapView);
        myloc = rootView.findViewById(R.id.myloc);
        listSwitch = rootView.findViewById(R.id.List_Switch);
        mRequestingLocationUpdates = true;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());


        mMapData = new HashMap<>();
        markerTheaterMap = new HashMap<>();

        /* Set up RecyclerView */
        mTheaters = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        int res2 = R.anim.layout_animation;
        LayoutAnimationController animation2 = AnimationUtils.loadLayoutAnimation(getContext(), res2);
        theatersMapViewRecycler = rootView.findViewById(R.id.recycler_view);
        theatersMapViewRecycler.setLayoutAnimation(animation2);
        theatersMapViewRecycler.setLayoutManager(mLayoutManager);
        theatersMapViewAdapter = new TheatersAdapter(mTheaters, this);

        //ListViewRecycler
        theatersListRecyclerview = rootView.findViewById(R.id.list_recycler);
        LinearLayoutManager vertical = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        theatersListRecyclerview.setLayoutManager(vertical);
        theatersListRecyclerview.setAdapter(theatersMapViewAdapter);


        mSearchClose.setOnClickListener(view -> {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                    .setCountry("US")
                    .build();
            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
                startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }
        });

        url = "http://moviepass.com/go/theaters";
        if(GoWatchItSingleton.getInstance().getCampaign()!=null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url+"/"+GoWatchItSingleton.getInstance().getCampaign();


        //Hide Keyboard when not in use
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ContextSingleton.getInstance(getContext()).getGlobalContext();


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        checkPermissions();
        updateLocationUI();


        listSwitch.setOnClickListener(v -> {
            if (theatersListRecyclerview.getVisibility() == View.GONE) {
                theatersListRecyclerview.setVisibility(View.VISIBLE);
                fadeOut(theatersMapViewRecycler);
                fadeIn(theatersListRecyclerview);
                theatersMapViewRecycler.setVisibility(View.GONE);
            } else {
                theatersListRecyclerview.setVisibility(View.GONE);
                fadeIn(theatersMapViewRecycler);
                fadeOut(theatersListRecyclerview);
                theatersMapViewRecycler.setVisibility(View.VISIBLE);
            }


        });


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
        if (checkPermissions()) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMinZoomPreference(9);
        //SNAP the recyclerview to center in the view
        LinearSnapHelper snapHelper = new LinearSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                View centerView = findSnapView(layoutManager);
                if (centerView != null)
                    return theatersMapViewRecycler.NO_POSITION;

                int position = 0;
                if (centerView != null) {
                    position = layoutManager.getPosition(centerView);
                }
                int targetPosition = -1;

                if (layoutManager.canScrollHorizontally()) {
                    if (velocityX < 5) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                if (layoutManager.canScrollVertically()) {
                    if (velocityY < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                final int firstItem = 0;
                final int lastItem = layoutManager.getItemCount() - 1;
                targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem));

                for (int i = 0; i < mTheaters.size(); i++) {
                    Double lat = mTheaters.get(i).getLat();
                    Double lon = mTheaters.get(i).getLon();
                    break;
                }


                return targetPosition;
            }
        };

        snapHelper.attachToRecyclerView(theatersMapViewRecycler);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style_json));
            mMap.getUiSettings().setMapToolbarEnabled(false);

            mClusterManager = new ClusterManager<>(getActivity(), mMap);
            mClusterManager.setRenderer(new TheaterPinRenderer());
            mMap.setOnMarkerClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(this);

            startLocationUpdates();
            updateLocationUI();

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
            mClusterManager.cluster();
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        mMap.setOnMarkerClickListener(marker -> {
            markerPosition = marker.getPosition();
            int theaterSelected = -1;

            for (int i = 0; i < mTheaters.size(); i++) {
                if (markerPosition.latitude == mTheaters.get(i).getLat() && markerPosition.longitude == mTheaters.get(i).getLon()) {
                    theaterSelected = i;
                }
            }
            //Onclick for individual Markers - adjusts recycler to that specific theater.
            CameraPosition theaterPosition = new CameraPosition.Builder().target(markerPosition).zoom(11).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(theaterPosition));
            theatersMapViewRecycler.getLayoutManager().scrollToPosition(theaterSelected);

            theatersListRecyclerview.getRecycledViewPool().clear();
            theatersMapViewAdapter.notifyDataSetChanged();

            marker.showInfoWindow();

            return true;
        });
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

        if (theatersMapViewRecycler.getVisibility() != View.VISIBLE && !mRequestingLocationUpdates) {
            loadTheaters(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        theatersMapViewRecycler.setVisibility(View.INVISIBLE);
        if(mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

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

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        theaterSelect = (OnTheaterSelect) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

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
            if (theatersMapViewRecycler.getVisibility() == View.VISIBLE) {
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
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location loc = task.getResult();

                    if (mRequestingLocationUpdates) {
                        if (loc != null) {
                            loadTheaters(loc.getLatitude(), loc.getLongitude());

                            LatLng coordinates = new LatLng(loc.getLatitude(), loc.getLongitude());
                            CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);
                            mMap.moveCamera(current);

                            lat = loc.getLatitude();
                            lon = loc.getLongitude();
                        }
                    }
                });
            } else {
//                requestPermissions();
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
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Location mCurrentLocation = task.getResult();
                updateLocationUI();
            }
        });
    }

    private void getMyLocation() {
        LatLng latLng = new LatLng(lat, lon);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.animateCamera(cameraUpdate);
        loadTheaters(lat, lon);
        theatersListRecyclerview.getRecycledViewPool().clear();
        theatersMapViewAdapter.notifyDataSetChanged();
        myloc.setVisibility(View.GONE);
    }


    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("GPS Services Are Required For MoviePass to Run Properlly")
                        .setMessage(" Allow GPS Location Access? ")
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS);
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        theatersListRecyclerview.getRecycledViewPool().clear();
        theatersMapViewAdapter.notifyDataSetChanged();
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mRequestingLocationUpdates = false;
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                GoWatchItSingleton.getInstance().searchEvent(place.getAddress().toString(),"theatrical_search",url);
                myloc.setVisibility(View.VISIBLE);
                loadTheaters(place.getLatLng().latitude, place.getLatLng().longitude);
                myloc.setOnClickListener(view -> {
                    mRequestingLocationUpdates = true;
                    getMyLocation();
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Toast.makeText(getActivity(), "You must grant permission to use MoviePass.", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(getActivity(), "You must grant permission to use MoviePass.", Toast.LENGTH_SHORT).show();

            }
        }
    }


    private void loadTheaters(Double latitude, final Double longitude) {
        mMap.clear();
        mTheaters.clear();
        mProgress.setVisibility(View.VISIBLE);
        theatersMapViewRecycler.setVisibility(View.VISIBLE);

        LatLng latlng = new LatLng(latitude, longitude);
        CameraUpdate current = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM_LEVEL);
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
                                mProgress.setVisibility(View.GONE);

                                Toast.makeText(getActivity(), "No Theaters found", Toast.LENGTH_SHORT).show();
                            } else {
                                mClusterManager.clearItems();
                                mClusterManager.cluster();

                                for (final Theater theater : theaterList) {
                                    LatLng location = new LatLng(theater.getLat(), theater.getLon());

                                    mMapData.put(location, theater);

                                    //Initial View to Display RecyclerView Based on User's Current Location
                                    mTheatersResponse = response.body();
                                    mTheaters.clear();
                                    if (theatersMapViewAdapter != null) {
                                        theatersMapViewRecycler.getRecycledViewPool().clear();
                                        theatersMapViewAdapter.notifyDataSetChanged();
                                    }

                                    if (mTheatersResponse != null) {
                                        Collections.sort(mTheaters, (theater1, t1) -> Double.compare(theater1.getDistance(), t1.getDistance()));
                                        mTheaters.addAll(mTheatersResponse.getTheaters());

                                        theatersMapViewRecycler.setAdapter(theatersMapViewAdapter);
                                        theatersMapViewRecycler.setTranslationY(0);
                                        theatersMapViewRecycler.setAlpha(1.0f);
                                        isRecyclerViewShown = true;
                                        mProgress.setVisibility(View.GONE);
                                    }
                                    final int position;
                                    position = theaterList.indexOf(theater);
                                    mClusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(), theater.getName(), R.drawable.theater_pin, position, theater));
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
                                            final Theater theaterMarker = markerTheaterMap.get(marker.getId());
                                            mProgress.setVisibility(View.VISIBLE);
                                            theatersMapViewRecycler.animate().translationY(theatersMapViewRecycler.getHeight()).alpha(0.5f).setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    Projection projection = mMap.getProjection();
                                                    LatLng markerLocation = finalMarker.getPosition();
                                                    Point screenPosition = projection.toScreenLocation(markerLocation);
                                                    onTheaterClick(position, theaterMarker, screenPosition.x, screenPosition.y);
                                                }
                                            });
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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private class TheaterPinRenderer extends DefaultClusterRenderer<TheaterPin> {
        private final IconGenerator mClusterIconGenerator;

        public TheaterPinRenderer() {
            super(getActivity(), mMap, mClusterManager);
            mClusterIconGenerator = new IconGenerator(getActivity());
        }

        @Override
        protected void onBeforeClusterItemRendered(TheaterPin theaterPin, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin)).title(theaterPin.getTitle());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<TheaterPin> cluster, MarkerOptions markerOptions) {
            try {
                mClusterIconGenerator.setBackground(
                        ContextCompat.getDrawable(getActivity(), R.drawable.icon_clustered_theater_pin));

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
            }
        }

        ;

        @Override
        protected void onClusterItemRendered(TheaterPin theaterPin, Marker marker) {
            super.onClusterItemRendered(theaterPin, marker);

            Theater theater = theaterPin.getTheater();
            markerTheaterMap.put(marker.getId(), theater);
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), (float) Math.floor(mMap.getCameraPosition().zoom + 1)), 300, null);
        return true;
    }

    public void onTheaterClick(int pos, Theater theater, int cx, int cy) {
        mClusterManager.clearItems();
        mClusterManager.cluster();
        if (theatersListRecyclerview.getVisibility() == View.VISIBLE) {
            theatersListRecyclerview.setVisibility(View.GONE);
            fadeOut(theatersListRecyclerview);
        }

        int recyclerViewHeight = theatersMapViewRecycler.getHeight();
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

//    public static void expand(final View v) {
//        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        final int targetedHeight = v.getMeasuredHeight();
//
//        v.getLayoutParams().height = 0;
//        v.setVisibility(View.VISIBLE);
//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                v.getLayoutParams().height = interpolatedTime == 1
//                        ? RelativeLayout.LayoutParams.WRAP_CONTENT
//                        : (int) (targetedHeight * interpolatedTime);
//                v.requestLayout();
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        a.setDuration((int) (targetedHeight / v.getContext().getResources().getDisplayMetrics().density));
//        v.startAnimation(a);
//    }
//
//    public void collapse(final View v) {
//        final int initialHeight = v.getMeasuredHeight();
//
//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                if (interpolatedTime == 1) {
//                    v.setVisibility(View.GONE);
//                } else {
//                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
//                    v.requestLayout();
//                }
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
//        v.startAnimation(a);
//    }

//    public void searchEvent(String search){
//
//        String l = String.valueOf(UserPreferences.getLatitude());
//        String ln = String.valueOf(UserPreferences.getLongitude());
//        String userId = String.valueOf(UserPreferences.getUserId());
//        String deep_link="";
//
//        String versionName = BuildConfig.VERSION_NAME;
//        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
//        String campaign = GoWatchItSingleton.getInstance().getCampaign();
//
//
//        RestClient.getAuthenticatedAPIGoWatchIt().searchTheatersMovies("theatrical_search","true",
//                "Movie","-1",search,campaign,"app","android",deep_link,"organic",
//                l,ln,userId,"IDFA", versionCode, versionName).enqueue(new RestCallback<GoWatchItResponse>() {
//            @Override
//            public void onResponse(Call<GoWatchItResponse> call, Response<GoWatchItResponse> response) {
//                GoWatchItResponse responseBody = response.body();
////                progress.setVisibility(View.GONE);
//
//                Log.d("HEADER SEARCH -- >", "onResponse: "+responseBody.getFollowUrl());
//            }
//
//            @Override
//            public void failure(RestError restError) {
////                progress.setVisibility(View.GONE);
//                // Toast.makeText(MovieActivity.this, restError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

}