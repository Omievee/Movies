package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.mobile.Constants;
import com.mobile.adapters.TheatersAdapter;
import com.mobile.helpers.ContextSingleton;
import com.mobile.model.Theater;
import com.mobile.model.TheaterPin;
import com.mobile.network.RestClient;
import com.mobile.responses.LocalStorageTheaters;
import com.moviepass.R;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;


public class TheatersFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, ClusterManager.OnClusterClickListener<TheaterPin>, LocationListener {

    Realm theatersRealm;


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    final static byte DEFAULT_ZOOM_LEVEL = 10;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final int LOCATION_PERMISSIONS = 99;
    public boolean expanded;
    private HashMap<LatLng, Theater> mMapData;
    private HashMap<String, Theater> markerTheaterMap;

    private GoogleApiClient mGoogleApiClient;
    private TheatersAdapter theaterAdapter;
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
    Location userCurrentLocation;
    private OnFragmentInteractionListener listener;

    RelativeLayout listViewMaps;
    public RelativeLayout goneList;
    ImageView mSearchClose, myloc;
    View mProgress;
    RelativeLayout mRelativeLayout;
    private ClusterManager<TheaterPin> clusterManager;
    @BindView(R.id.listViewTheaters)
    RecyclerView theatersRECY;
    RealmList<Theater> myList;
    String TAG = "TAG";
    int finalK;
    double meterToMile;
    ArrayList<Theater> nearbyTheaters;
    ArrayList<Theater> closestTheaters;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_theaters, container, false);
        ButterKnife.bind(this, rootView);

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_theaters, container, false);
        }

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
        mRequestingLocationUpdates = true;
        listViewMaps = rootView.findViewById(R.id.ListViewMaps);
        goneList = rootView.findViewById(R.id.goneList);
        nearbyTheaters = new ArrayList<>();

        mMapData = new HashMap<>();
        markerTheaterMap = new HashMap<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        /* Set up RecyclerView */
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        theatersRECY = rootView.findViewById(R.id.listViewTheaters);
        theatersRECY.setLayoutManager(manager);
        theaterAdapter = new TheatersAdapter(nearbyTheaters);
        theatersRECY.setAdapter(theaterAdapter);


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

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                expand(goneList);
//            }
//        }, 500);


        listViewMaps.setOnClickListener(v -> {
            if (goneList.getVisibility() == View.GONE) {
                expand(goneList);
                expanded = true;
            } else {
                expanded = false;
                collapse(goneList);
            }
        });


        ContextSingleton.getInstance(getContext()).getGlobalContext();


        theatersRealm = Realm.getDefaultInstance();
        //getAllTheatersForStorage();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        buildLocationSettingsRequest();
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
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMinZoomPreference(9);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style_json));
            mMap.getUiSettings().setMapToolbarEnabled(false);
            clusterManager = new ClusterManager<>(getActivity(), mMap);
            clusterManager.setRenderer(new TheaterPinRenderer());
            mMap.setOnMarkerClickListener(clusterManager);
            clusterManager.setOnClusterClickListener(this);
            clusterManager.cluster();
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }


//        mMap.setOnMarkerClickListener(marker -> {
//            markerPosition = marker.getPosition();
//            int theaterSelected = -1;
//
//            for (int i = 0; i < mTheaters.size(); i++) {
//                if (markerPosition.latitude == mTheaters.get(i).getLat() && markerPosition.longitude == mTheaters.get(i).getLon()) {
//                    theaterSelected = i;
//                }
//            }
//            //Onclick for individual Markers - adjusts recycler to that specific theater.
//            CameraPosition theaterPosition = new CameraPosition.Builder().target(markerPosition).zoom(11).build();
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(theaterPosition));
//            theatersRECY.getLayoutManager().scrollToPosition(theaterSelected);
//
//            theatersListRecyclerview.getRecycledViewPool().clear();
//            theaterAdapter.notifyDataSetChanged();
//
//            marker.showInfoWindow();
//
//            return true;
//        });
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();

        if (clusterManager != null) {
            clusterManager.clearItems();
            clusterManager.cluster();
        }
        locationUpdateRealm();
    }

    void locationUpdateRealm() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS);
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location loc = task.getResult();
            if (mRequestingLocationUpdates) {
                if (loc != null) {
                    mMap.setMyLocationEnabled(true);
                    Log.d(TAG, "*******HIT******: ");

                    queryRealmLoadTheaters(loc.getLatitude(), loc.getLongitude());

                    LatLng coordinates = new LatLng(loc.getLatitude(), loc.getLongitude());
                    CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);
                    mMap.moveCamera(current);
                    lat = Double.parseDouble(String.format("%.2f", loc.getLatitude()));
                    lon = Double.parseDouble(String.format("%.2f", loc.getLongitude()));
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        theatersRECY.setVisibility(View.INVISIBLE);
        if (clusterManager != null) {
            clusterManager.clearItems();
            clusterManager.cluster();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        if (clusterManager != null) {
            clusterManager.clearItems();
            clusterManager.cluster();
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (clusterManager != null) {
            clusterManager.clearItems();
            clusterManager.cluster();
        }

        listener = null;
    }

    private void createLocationCallback() {
        Log.d(TAG, "HIT?: ");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                Log.d(TAG, "onLocationResult: " + mCurrentLocation);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        };
    }


    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


//TODO BRING BACK
//    private void getMyLocation() {
//        LatLng latLng = new LatLng(lat, lon);
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
//        mMap.animateCamera(cameraUpdate);
//        loadTheaters(lat, lon);
//        theatersListRecyclerview.getRecycledViewPool().clear();
//        theaterAdapter.notifyDataSetChanged();
//        myloc.setVisibility(View.GONE);
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        theaterAdapter.notifyDataSetChanged();
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mRequestingLocationUpdates = false;
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                myloc.setVisibility(View.VISIBLE);
                //  realm(place.getLatLng().latitude, place.getLatLng().longitude);
                myloc.setOnClickListener(view -> {
                    mRequestingLocationUpdates = true;
                });
            }
        }
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
            super(getActivity(), mMap, clusterManager);
            mClusterIconGenerator = new IconGenerator(getActivity());
        }

        @Override
        protected void onBeforeClusterItemRendered(TheaterPin theaterPin, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_clustered_theater_pin)).title(theaterPin.getTitle());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<TheaterPin> cluster, MarkerOptions markerOptions) {
            try {
                mClusterIconGenerator.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.icon_clustered_theater_pin));

                mClusterIconGenerator.setTextAppearance(R.style.ThemeOverlay_AppCompat_Dark);

                final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

                // Draw multiple people.
                // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
                List<Drawable> theaterPins = new ArrayList<>(Math.min(3, cluster.getSize()));

                for (TheaterPin p : cluster.getItems()) {
                    // Draw 4 at most.
                    if (theaterPins.size() == 4) break;
                    Drawable drawable = getResources().getDrawable(R.drawable.theaterpineticket);
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


    public interface OnFragmentInteractionListener {
    }


    void queryRealmLoadTheaters(double latitude, double longitude) {
        nearbyTheaters.clear();
        userCurrentLocation = new Location(LocationManager.GPS_PROVIDER);
        userCurrentLocation.setLatitude(latitude);
        userCurrentLocation.setLongitude(longitude);
        RealmResults<Theater> allTheaters = theatersRealm.where(Theater.class).findAll();

        for (int K = 0; K < allTheaters.size(); K++) {
            Location pointB = new Location(LocationManager.GPS_PROVIDER);
            pointB.setLatitude(allTheaters.get(K).getLat());
            pointB.setLongitude(allTheaters.get(K).getLon());
            double disntanceTO = userCurrentLocation.distanceTo(pointB);

            Collections.sort(nearbyTheaters, (o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));
            if (disntanceTO <= 48280.3) {
                nearbyTheaters.add(allTheaters.get(K));
            }

        }
        for (int j = 0; j < nearbyTheaters.size(); j++) {
            Location localPoints = new Location(LocationManager.GPS_PROVIDER);
            localPoints.setLatitude(nearbyTheaters.get(j).getLat());
            localPoints.setLongitude(nearbyTheaters.get(j).getLon());

            double d = userCurrentLocation.distanceTo(localPoints);
            double mtrMLE = (d / 1609.344);
            theatersRealm.beginTransaction();
            nearbyTheaters.get(j).setDistance(Double.parseDouble(String.format("%.2f", mtrMLE)));
            theatersRealm.commitTransaction();

            Log.d(TAG, "queryRealmLoadTheaters: " + nearbyTheaters.get(j).getDistance() + "   " + nearbyTheaters.get(j).getName());
        }


        if (nearbyTheaters.size() > 40) {
            nearbyTheaters.subList(40, nearbyTheaters.size()).clear();
        }

        for (int i = 0; i < nearbyTheaters.size(); i++) {
            Log.d(TAG, "queryRealmLoadTheaters: " + nearbyTheaters.get(i).getName());
        }


        if (nearbyTheaters.size() == 0) {
            Toast.makeText(getActivity(), "No Theaters found", Toast.LENGTH_SHORT).show();
        } else {
            clusterManager.clearItems();
            clusterManager.cluster();

            for (final Theater theater : nearbyTheaters) {
                LatLng location = new LatLng(theater.getLat(), theater.getLon());

                mMapData.put(location, theater);

                Log.d(TAG, "queryRealmLoadTheaters: " + theater.getLat() + " " + theater.getLon());

                final int position;
                position = nearbyTheaters.indexOf(theater);

                if (theater.ticketTypeIsETicket() || theater.ticketTypeIsSelectSeating()) {
                    clusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(), theater.getName(), R.drawable.theaterpineticket, position, theater));
                } else {
                    clusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(), theater.getName(), R.drawable.theaterpinstandard, position, theater));
                }
                mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
                final CameraPosition[] mPreviousCameraPosition = {null};

                mMap.setOnCameraIdleListener(() -> {
                    CameraPosition position1 = mMap.getCameraPosition();
                    if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position1.zoom) {
                        mPreviousCameraPosition[0] = mMap.getCameraPosition();
                        clusterManager.cluster();
                    }
                });
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        theatersRealm.close();
    }


//    private void loadTheaters(Double latitude, final Double longitude) {
//        Log.d(TAG, "****Begin****: ");
//        mMap.clear();
//        mTheaters.clear();
////        mProgress.setVisibility(View.VISIBLE);
//        theatersRECY.setVisibility(View.VISIBLE);
//
//        LatLng latlng = new LatLng(latitude, longitude);
//        CameraUpdate current = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM_LEVEL);
//        mMap.moveCamera(current);
//        mMap.animateCamera(current);
//
//
//        Log.d(TAG, "****END****: ");
//
//        RestClient.getAuthenticated().getTheaters(latitude, longitude).enqueue(new Callback<TheatersResponse>() {
//            @Override
//            public void onResponse(Call<TheatersResponse> call, final Response<TheatersResponse> response) {
//                TheatersResponse theaters = response.body();
//                if (theaters != null) {
//                    List<Theater> theaterList = theaters.getTheaters();
//                    if (theaterList.size() == 0) {
//                        mProgress.setVisibility(View.GONE);
//
//                        Toast.makeText(getActivity(), "No Theaters found", Toast.LENGTH_SHORT).show();
//                    } else {
//                        clusterManager.clearItems();
//                        clusterManager.cluster();
//
//                        for (final Theater theater : theaterList) {
//                            LatLng location = new LatLng(theater.getLat(), theater.getLon());
//
//                            mMapData.put(location, theater);
//
//                            //Initial View to Display RecyclerView Based on User's Current Location
//                            mTheatersResponse = response.body();
//                            mTheaters.clear();
//                            if (theaterAdapter != null) {
//                                theatersRECY.getRecycledViewPool().clear();
//                                theaterAdapter.notifyDataSetChanged();
//                            }
//
//                            if (mTheatersResponse != null) {
//                                Collections.sort(mTheaters, (theater1, t1) -> Double.compare(theater1.getDistance(), t1.getDistance()));
//                                mTheaters.addAll(mTheatersResponse.getTheaters());
//
//                                theatersRECY.setAdapter(theaterAdapter);
//                                theatersRECY.setTranslationY(0);
//                                theatersRECY.setAlpha(1.0f);
//                                isRecyclerViewShown = true;
//                                mProgress.setVisibility(View.GONE);
//                            }
//                            final int position;
//                            position = theaterList.indexOf(theater);
//                            clusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(), theater.getName(), R.drawable.theater_pin, position, theater));
//                            mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
//                            final CameraPosition[] mPreviousCameraPosition = {null};
//
//                            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//                                @Override
//                                public void onCameraIdle() {
//                                    CameraPosition position = mMap.getCameraPosition();
//                                    if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
//                                        mPreviousCameraPosition[0] = mMap.getCameraPosition();
//                                        clusterManager.cluster();
//                                    }
//                                }
//                            });
//
//                            clusterManager.cluster();
//
//                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                                @Override
//                                public void onInfoWindowClick(final Marker marker) {
//                                    mRequestingLocationUpdates = false;
//                                    final Marker finalMarker = marker;
//                                    final Theater theaterMarker = markerTheaterMap.get(marker.getId());
//                                    mProgress.setVisibility(View.VISIBLE);
//                                    theatersRECY.animate().translationY(theatersRECY.getHeight()).alpha(0.5f).setListener(new AnimatorListenerAdapter() {
//                                        @Override
//                                        public void onAnimationEnd(Animator animation) {
//                                            super.onAnimationEnd(animation);
//                                            Projection projection = mMap.getProjection();
//                                            LatLng markerLocation = finalMarker.getPosition();
//                                            Point screenPosition = projection.toScreenLocation(markerLocation);
//                                            onTheaterClick(position, theaterMarker, screenPosition.x, screenPosition.y);
//                                        }
//                                    });
//                                }
//                            });
//
//
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<TheatersResponse> call, Throwable t) {
//                if (t != null) {
//                    Log.d("Unable to get theaters", "Unable to download theaters: " + t.getMessage());
//                }
//            }
//
//        });
//    }


    /**
     * REALM CODE
     */

    public void getAllTheatersForStorage() {
        RestClient.getAllTheatersAPI().getAllMoviePassTheaters().enqueue(new Callback<LocalStorageTheaters>() {
            @Override
            public void onResponse(Call<LocalStorageTheaters> call, Response<LocalStorageTheaters> response) {
                LocalStorageTheaters locallyStoredTheaters = response.body();
                if (locallyStoredTheaters != null && response.isSuccessful()) {
                    for (int i = 0; i < locallyStoredTheaters.getTheaters().size(); i++) {
                        Log.d(TAG, "onResponse: " + locallyStoredTheaters.getTheaters().get(i).getTribuneTheaterId());
                    }
                    theatersRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(@NonNull Realm R) {
                            for (int j = 0; j < locallyStoredTheaters.getTheaters().size(); j++) {
                                Theater RLMTH = R.createObject(Theater.class);
                                RLMTH.setId(locallyStoredTheaters.getTheaters().get(j).getId());
                                RLMTH.setMoviepassId(locallyStoredTheaters.getTheaters().get(j).getMoviepassId());
                                RLMTH.setTribuneTheaterId(locallyStoredTheaters.getTheaters().get(j).getTribuneTheaterId());
                                RLMTH.setName(locallyStoredTheaters.getTheaters().get(j).getName());
                                RLMTH.setAddress(locallyStoredTheaters.getTheaters().get(j).getAddress());
                                RLMTH.setCity(locallyStoredTheaters.getTheaters().get(j).getCity());
                                RLMTH.setState(locallyStoredTheaters.getTheaters().get(j).getState());
                                RLMTH.setZip(locallyStoredTheaters.getTheaters().get(j).getZip());
                                RLMTH.setDistance(locallyStoredTheaters.getTheaters().get(j).getDistance());
                                RLMTH.setLat(locallyStoredTheaters.getTheaters().get(j).getLat());
                                RLMTH.setLon(locallyStoredTheaters.getTheaters().get(j).getLon());
                                RLMTH.setTicketType(locallyStoredTheaters.getTheaters().get(j).getTicketType());
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: ");
                            locationUpdateRealm();

                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            // Transaction failed and was automatically canceled.
                            Log.d(TAG, "Realm onError: " + error.getMessage());
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<LocalStorageTheaters> call, Throwable t) {
                Toast.makeText(getContext(), "Error while downloading Theaters.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, 200);
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

    public void collapse(final View v) {
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