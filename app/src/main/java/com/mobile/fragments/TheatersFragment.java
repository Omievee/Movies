package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Places;
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
import com.helpshift.support.Log;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.SimpleOnSearchActionListener;
import com.mobile.Constants;
import com.mobile.UserLocationManagerFused;
import com.mobile.activities.TheaterActivity;
import com.mobile.adapters.TheatersAdapter;
import com.mobile.helpers.ContextSingleton;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.model.Theater;
import com.mobile.model.TheaterPin;
import com.mobile.network.RestClient;
import com.mobile.responses.LocalStorageTheaters;
import com.moviepass.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TheatersFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, ClusterManager.OnClusterClickListener<TheaterPin>, LocationListener {


    public static final String GCM_ONEOFF_TAG = "oneoff|[0,0]";
    public static final String GCM_REPEAT_TAG = "repeat|[7200,1800]";

    private final static String senderID = "11111111111";
    public static Realm tRealm;
    final static byte DEFAULT_ZOOM_LEVEL = 10;
    public static final int LOCATION_PERMISSIONS = 99;
    public boolean expanded;
    private HashMap<LatLng, Theater> mMapData;
    private HashMap<String, Theater> markerTheaterMap;
    Context myContext;
    Activity myActivity;
    FragmentActivity myFragment;
    private GoogleApiClient mGoogleApiClient;
    private TheatersAdapter theaterAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Boolean mRequestingLocationUpdates;
    double lat, lon;
    GoogleMap mMap;
    MapView mMapView;
    String url;
    Location userCurrentLocation;
    MaterialSearchBar searchGP;
    Button searchThisArea;
    RelativeLayout listViewMaps, mRelativeLayout;
    RelativeLayout goneList;
    ImageView mSearchClose, myloc, upArrow, downArrow;
    View mProgress;
    TextView listViewText, mapViewText;
    ClusterManager<TheaterPin> mClusterManager;
    RecyclerView theatersRECY;
    String TAG = "TAG";
    LinkedList<Theater> nearbyTheaters;
    SlidingUpPanelLayout slideup;
    double furthest, LAT, LON;
    Location localPoints;
    Location smallLocal;
    View customInfoWindow;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_theaters, container, false);
        ButterKnife.bind(this, rootView);

        rootView = inflater.inflate(R.layout.fragment_theaters, container, false);

        mGoogleApiClient = new GoogleApiClient
                .Builder(myContext)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();


        searchGP = rootView.findViewById(R.id.SearchGP);
        mRelativeLayout = rootView.findViewById(R.id.relative_layout);
        mSearchClose = rootView.findViewById(R.id.search_inactive);
        mProgress = rootView.findViewById(R.id.progress);
        mMapView = rootView.findViewById(R.id.MPMAPVIEW);
        myloc = rootView.findViewById(R.id.myloc);
        mRequestingLocationUpdates = true;
        listViewMaps = rootView.findViewById(R.id.ListViewMaps);
        goneList = rootView.findViewById(R.id.goneList);
        nearbyTheaters = new LinkedList<>();
        mMapData = new HashMap<>();
        markerTheaterMap = new HashMap<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(myContext);
        slideup = rootView.findViewById(R.id.sliding_layout);
        /* Set up RecyclerView */

        LinearLayoutManager manager = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        theatersRECY = rootView.findViewById(R.id.listViewTheaters);
        theatersRECY.setLayoutManager(manager);
        theaterAdapter = new TheatersAdapter(nearbyTheaters);
        theatersRECY.setAdapter(theaterAdapter);
        searchThisArea = rootView.findViewById(R.id.SearchThisArea);
        listViewText = rootView.findViewById(R.id.ListViewText);
        upArrow = rootView.findViewById(R.id.uparrow);
        downArrow = rootView.findViewById(R.id.downarrow);
        mapViewText = rootView.findViewById(R.id.mapviewtext);


        url = "http://moviepass.com/go/theaters";
        if (GoWatchItSingleton.getInstance().getCampaign() != null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();


        //Hide Keyboard when not in use
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ContextSingleton.getInstance(getContext()).getGlobalContext();


//        tRealm = Realm.getDefaultInstance();
        mSearchClose.setOnClickListener(view -> {

            searchGP.enableSearch();
            searchGP.setMaxSuggestionCount(0);
            fadeIn(searchGP);
            searchGP.setVisibility(View.VISIBLE);
            fadeOut(mSearchClose);
            mSearchClose.setVisibility(View.INVISIBLE);
            searchGP.animate().start();
            searchGP.setOnSearchActionListener(new SimpleOnSearchActionListener() {
                @Override
                public void onSearchConfirmed(CharSequence text) {
                    super.onSearchConfirmed(text);
                    searchMap(text.toString());
                    fadeOut(searchGP);
                    searchGP.setVisibility(View.GONE);
                    fadeIn(mSearchClose);
                    mSearchClose.setVisibility(View.VISIBLE);
                }
            });
        });

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        myloc.setOnClickListener(v -> {
            mRequestingLocationUpdates = true;
            getMyLocation();
        });

        buildLocationSettingsRequest();
        Log.d(TAG, "onViewCreated: " + slideup.getPanelState());


        slideup.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (slideup.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    fadeIn(downArrow);
                    downArrow.setVisibility(View.VISIBLE);
                    fadeIn(mapViewText);
                    mapViewText.setVisibility(View.VISIBLE);

                    fadeOut(listViewText);
                    listViewText.setVisibility(View.INVISIBLE);
                    fadeOut(upArrow);
                    upArrow.setVisibility(View.INVISIBLE);

                    String url = "http://moviepass.com/go/list";
                    GoWatchItSingleton.getInstance().userOpenedTheaterTab(url, "list_view_click");

                } else {
                    fadeOut(downArrow);
                    downArrow.setVisibility(View.GONE);

                    fadeOut(mapViewText);
                    mapViewText.setVisibility(View.GONE);
                    fadeIn(listViewText);
                    listViewText.setVisibility(View.VISIBLE);
                    fadeIn(upArrow);
                    upArrow.setVisibility(View.VISIBLE);
                }
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
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMinZoomPreference(1);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.

            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(myContext, R.raw.map_style_json));
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mClusterManager = new ClusterManager<>(myContext, mMap);
            mClusterManager.setRenderer(new TheaterPinRenderer());
            mMap.setOnMarkerClickListener(mClusterManager);


            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.cluster();
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        tRealm = Realm.getDefaultInstance();

        if (tRealm.isEmpty()) {
            getAllTheatersForStorage();
        } else {
            locationUpdateRealm();
        }
        mProgress.setVisibility(View.VISIBLE);
        customInfoWindow = View.inflate(myActivity, R.layout.fr_theaters_infowindow, null);
        mMap.setOnMarkerClickListener(marker -> {
            ImageView etickIcon = customInfoWindow.findViewById(R.id.info_Etix);
            ImageView seatIcon = customInfoWindow.findViewById(R.id.info_Seat);
            etickIcon.setVisibility(View.GONE);
            seatIcon.setVisibility(View.GONE);
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    TextView infoTheaterName = customInfoWindow.findViewById(R.id.info_TheaterName);
                    TextView infoTheaterAddress1 = customInfoWindow.findViewById(R.id.info_TheaterAddress1);
                    TextView infoTheaterAddress2 = customInfoWindow.findViewById(R.id.info_TheaterAddress2);

                    ImageView etickIcon = customInfoWindow.findViewById(R.id.info_Etix);
                    ImageView seatIcon = customInfoWindow.findViewById(R.id.info_Seat);

                    infoTheaterName.setText(marker.getTitle());

                    String address = marker.getSnippet();
                    List<String> snippetString = Arrays.asList(address.split(",", -1));

                    for (int i = 0; i < snippetString.size(); i++) {
                        infoTheaterAddress1.setText(snippetString.get(0).trim());
                        infoTheaterAddress2.setText(snippetString.get(1).trim());
                    }


                    if (marker.getTitle() != null) {
                        if (markerTheaterMap.get(marker.getId()).getTicketType().matches("E_TICKET")) {
                            etickIcon.setVisibility(View.VISIBLE);
                        } else if (markerTheaterMap.get(marker.getId()).getTicketType().matches("SELECT_SEATING")) {
                            etickIcon.setVisibility(View.VISIBLE);
                            seatIcon.setVisibility(View.VISIBLE);
                        }
                    }
                    return customInfoWindow;
                }
            });


            if (marker.getTitle() != null) {
                LatLng latLng = new LatLng(markerTheaterMap.get(marker.getId()).getLat(), markerTheaterMap.get(marker.getId()).getLon());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                mMap.animateCamera(cameraUpdate);
                marker.showInfoWindow();
            }
            return true;
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            mProgress.setVisibility(View.VISIBLE);
            Intent intent = new Intent(myContext, TheaterActivity.class);
            intent.putExtra("cinema", Parcels.wrap(Theater.class, markerTheaterMap.get(marker.getId())));
            myContext.startActivity(intent);
        });

    }


    void locationUpdateRealm() {
        if (ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(myActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS);
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location loc = task.getResult();
            if (mRequestingLocationUpdates) {
                if (loc != null) {
                    mMap.setMyLocationEnabled(true);
                    Log.d(TAG, "*******HIT******: ");
                    lat = Double.parseDouble(String.format(Locale.getDefault(), "%.2f", loc.getLatitude()));
                    lon = Double.parseDouble(String.format(Locale.getDefault(), "%.2f", loc.getLongitude()));

                    queryRealmLoadTheaters(lat, lon);
                    LatLng coordinates = new LatLng(loc.getLatitude(), loc.getLongitude());
                    CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);
                    mMap.moveCamera(current);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
   //     locationUpdateRealm();
    }


    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    private void getMyLocation() {
        boolean enabled = UserLocationManagerFused.getLocationInstance(myContext).isLocationEnabled();
        if (!enabled) {
            EnableLocation location = new EnableLocation();
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            location.show(fm, "fr_enablelocation");
        }
        mProgress.setVisibility(View.VISIBLE);
        LatLng latLng = new LatLng(lat, lon);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
        mMap.animateCamera(cameraUpdate);
        queryRealmLoadTheaters(lat, lon);
        Log.d(TAG, "getMyLocation:  " + lat + "  " + lon);
        theatersRECY.getRecycledViewPool().clear();
        theaterAdapter.notifyDataSetChanged();
        if (searchThisArea.getVisibility() == View.VISIBLE) {
            searchThisArea.setVisibility(View.GONE);
            fadeOut(searchThisArea);
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
            super(myContext, mMap, mClusterManager);
            mClusterIconGenerator = new IconGenerator(myContext);
        }

        @Override
        protected void onBeforeClusterItemRendered(TheaterPin theaterPin, MarkerOptions markerOptions) {
            if (theaterPin.getTheater().ticketTypeIsSelectSeating() || theaterPin.getTheater().ticketTypeIsETicket()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.eticketingpin)).title(theaterPin.getTitle()).snippet(theaterPin.getSnippet());
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.post_pin)).title(theaterPin.getTitle()).snippet(theaterPin.getSnippet());
            }
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<TheaterPin> cluster, MarkerOptions markerOptions) {
            try {
                mClusterIconGenerator.setBackground(ContextCompat.getDrawable(myActivity, R.drawable.icon_clustered_theater_pin));
                mClusterIconGenerator.setTextAppearance(R.style.ThemeOverlay_AppCompat_Dark);

                final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

                // Draw multiple people.
                // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
                List<Drawable> theaterPins = new ArrayList<>(Math.min(3, cluster.getSize()));
                for (TheaterPin p : cluster.getItems()) {
                    // Draw 4 at most.
                    if (theaterPins.size() == 0) break;
                    Drawable drawable = getResources().getDrawable(R.drawable.theaterpineticket);
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


    void queryRealmLoadTheaters(double newLat, double newLong) {
        nearbyTheaters.clear();
        userCurrentLocation = new Location(LocationManager.GPS_PROVIDER);
        userCurrentLocation.setLatitude(newLat);
        userCurrentLocation.setLongitude(newLong);

        RealmResults<Theater> allTheaters = tRealm.where(Theater.class).findAll();

        Log.d(TAG, "THEATERS SIZE???!?!?!?!?: " + allTheaters.size());
        for (int K = 0; K < allTheaters.size(); K++) {
            Location pointB = new Location(LocationManager.GPS_PROVIDER);

            pointB.setLatitude(allTheaters.get(K).getLat());
            pointB.setLongitude(allTheaters.get(K).getLon());

            double disntanceTO = userCurrentLocation.distanceTo(pointB);
            if (disntanceTO <= 48280.3) {
                nearbyTheaters.add(allTheaters.get(K));
            }
        }
        for (int j = 0; j < nearbyTheaters.size(); j++) {
            localPoints = new Location(LocationManager.GPS_PROVIDER);
            localPoints.setLatitude(nearbyTheaters.get(j).getLat());
            localPoints.setLongitude(nearbyTheaters.get(j).getLon());

            double d = userCurrentLocation.distanceTo(localPoints);
            double mtrMLE = (d / 1609.344);
            tRealm.beginTransaction();
            nearbyTheaters.get(j).setDistance(Double.parseDouble(String.format("%.2f", mtrMLE)));
            tRealm.commitTransaction();
        }
        //Sort through shorter list..
        Collections.sort(nearbyTheaters, (o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));
        if (nearbyTheaters.size() > 40) {
            nearbyTheaters.subList(40, nearbyTheaters.size()).clear();

        }


        for (int i = nearbyTheaters.size() - 1; i > 0; i--) {
            smallLocal = new Location(LocationManager.GPS_PROVIDER);
            smallLocal.setLatitude(nearbyTheaters.get(i).getLat());
            smallLocal.setLongitude(nearbyTheaters.get(i).getLon());

            furthest = userCurrentLocation.distanceTo(smallLocal);
            Theater etixSelect = nearbyTheaters.get(i);

            if (etixSelect.getTicketType().matches("E_TICKET") || etixSelect.getTicketType().matches("SELECT_SEATING")) {
                nearbyTheaters.remove(etixSelect);
                nearbyTheaters.add(0, etixSelect);
            }

            mMap.setOnCameraMoveListener(() -> {
                Location cameraLocal = new Location(LocationManager.GPS_PROVIDER);
                cameraLocal.setLatitude(Double.parseDouble(String.format("%.2f", mMap.getCameraPosition().target.latitude)));
                cameraLocal.setLongitude(Double.parseDouble(String.format("%.2f", mMap.getCameraPosition().target.longitude)));


                double distance = userCurrentLocation.distanceTo(cameraLocal);
                double myLocationToCameraLocation = (distance / 1609.344);
                double myLocationToFurthestTheaterLocation = (furthest / 1609.344);

                if (myLocationToCameraLocation > myLocationToFurthestTheaterLocation) {
                    fadeIn(searchThisArea);
                    searchThisArea.setVisibility(View.VISIBLE);
                    searchThisArea.setOnClickListener(v -> {
                        double searchLat = Double.parseDouble(String.format("%.2f", mMap.getCameraPosition().target.latitude));
                        double searchLon = Double.parseDouble(String.format("%.2f", mMap.getCameraPosition().target.longitude));
                        mProgress.setVisibility(View.VISIBLE);
                        queryRealmLoadTheaters(searchLat, searchLon);
                        searchThisArea.setVisibility(View.GONE);
                        fadeOut(searchThisArea);
                    });
                }
            });

        }

        displayTheatersFromRealm(nearbyTheaters);

    }

    void displayTheatersFromRealm(LinkedList<Theater> theatersList) {
        mProgress.setVisibility(View.GONE);


        theaterAdapter.notifyDataSetChanged();
        slideup.setEnabled(true);
        listViewText.setTextColor(getResources().getColor(R.color.white));
        upArrow.setColorFilter(getResources().getColor(R.color.white));

        mClusterManager.clearItems();
        for (Theater theater : theatersList) {
            LatLng location = new LatLng(theater.getLat(), theater.getLon());
            mMapData.put(location, theater);
            final int position;
            position = theatersList.indexOf(theater);

            mClusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(), theater.getName(), R.drawable.theaterpinstandard, position, theater));
            mClusterManager.cluster();

            final CameraPosition[] mPreviousCameraPosition = {null};
            mMap.setOnCameraIdleListener(() -> {
                CameraPosition position1 = mMap.getCameraPosition();
                if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position1.zoom) {
                    mPreviousCameraPosition[0] = mMap.getCameraPosition();
                    mClusterManager.cluster();
                }
            });
        }
        mClusterManager.cluster();

        Log.d(TAG, "displayTheatersFromRealm: " + theatersList.size());


        Handler han = new Handler();
        han.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (theatersList.size() == 0) {
                    slideup.setEnabled(false);
                    listViewText.setTextColor(getResources().getColor(R.color.gray_icon));
                    upArrow.setColorFilter(getResources().getColor(R.color.gray_icon));
                    Toast.makeText(myActivity, "No Theaters found", Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000);

    }


    void searchMap(String searchString) {
        Geocoder geo = new Geocoder(myContext);
        try {
            List<Address> addresses = geo.getFromLocationName(searchString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                GoWatchItSingleton.getInstance().searchEvent(address.toString(), "theatrical_search", url);
                queryRealmLoadTheaters(address.getLatitude(), address.getLongitude());
                theaterAdapter.notifyDataSetChanged();
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
                mMap.animateCamera(cameraUpdate);
                Log.d(TAG, "address: " + address);
            } else {
                RealmResults<Theater> searchArea = tRealm.where(Theater.class)
                        .contains("city", searchString)
                        .findAll();
                GoWatchItSingleton.getInstance().searchEvent(searchString, "theatrical_search", url);

                for (int i = 0; i < searchArea.size(); i++) {
                    LAT = searchArea.get(i).getLat();
                    LON = searchArea.get(i).getLon();

                    Log.d(TAG, "searchMap: " + LAT);
                    Log.d(TAG, "searchMap: " + LON);
                }
                if (LAT != 0.0 && LON != 0.0) {
                    queryRealmLoadTheaters(LAT, LON);
                    theaterAdapter.notifyDataSetChanged();
                    LatLng latLng = new LatLng(LAT, LON);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
                    mMap.animateCamera(cameraUpdate);
                } else {
                    Toast.makeText(myContext, "No Theaters found", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        tRealm.close();
    }


    /**
     * REALM CODE
     */


    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(200);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(200);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }

    public void getAllTheatersForStorage() {
        RestClient.getLocalStorageAPI().getAllMoviePassTheaters().enqueue(new Callback<LocalStorageTheaters>() {
            @Override
            public void onResponse(Call<LocalStorageTheaters> call, Response<LocalStorageTheaters> response) {
                LocalStorageTheaters locallyStoredTheaters = response.body();
                if (locallyStoredTheaters != null && response.isSuccessful()) {
                    tRealm.executeTransactionAsync(R -> {

                        for (int j = 0; j < locallyStoredTheaters.getTheaters().size(); j++) {
                            Theater RLMTH = R.createObject(Theater.class, locallyStoredTheaters.getTheaters().get(j).getId());
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
                    }, () -> {
                        Log.d(Constants.TAG, "onSuccess: ");
                        locationUpdateRealm();
                    }, error -> {
                        // Transaction failed and was automatically canceled.
                        Log.d(Constants.TAG, "Realm onError: " + error.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Call<LocalStorageTheaters> call, Throwable t) {
                Toast.makeText(myActivity, "Error while downloading Theaters.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}