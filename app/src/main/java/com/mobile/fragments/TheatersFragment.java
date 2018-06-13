package com.mobile.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.SimpleOnSearchActionListener;
import com.mobile.Constants;
import com.mobile.adapters.EticketTheatersAdapter;
import com.mobile.adapters.TheatersAdapter;
import com.mobile.helpers.GoWatchItSingleton;
import com.mobile.helpers.LogUtils;
import com.mobile.listeners.TheatersClickListener;
import com.mobile.location.UserLocation;
import com.mobile.model.Header;
import com.mobile.model.Theater;
import com.mobile.model.TheaterPin;
import com.mobile.network.RestClient;
import com.mobile.responses.LocalStorageTheaters;
import com.mobile.rx.Schedulers;
import com.moviepass.BuildConfig;
import com.moviepass.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;


public class TheatersFragment extends MPFragment implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<TheaterPin>, TheatersClickListener {


    public static final String GCM_ONEOFF_TAG = "oneoff|[0,0]";
    public static final String GCM_REPEAT_TAG = "repeat|[7200,1800]";

    private final static String senderID = "11111111111";
    @Inject
    com.mobile.location.LocationManager locationManager;


    public GoogleMap googleMap;
    MapView mapView;
    public static ClusterManager<TheaterPin> theaterClusterManager;
    public static double LAT, LON;
    public FusedLocationProviderClient fusedLocationClient;
    //Views
    public ImageView searchIcon, myCurrentLocationButton, upArrow, downArrow, zoomOnCurrentLocationButton;
    public TextView listViewText, mapViewText;
    public Button searchThisArea;
    public MaterialSearchBar searchLocation;
    View progressLoader, customInfoWindow;
    public RelativeLayout listViewMaps, goneList;
    public SlidingUpPanelLayout theatersListView;
    public static final String THEATER_CHILD = "theaterFragment";

    //Lists
    public HashMap<LatLng, Theater> mapData;
    public HashMap<String, Theater> markerTheaterMap;
    public RecyclerView theatersRecyclerView;
    public RecyclerView eTicketTheatersRecyclerView;

    public TheatersAdapter theatersAdapter;
    public EticketTheatersAdapter eticketTheatersAdapter;
    public LinkedList<Theater> nearbyTheaters;
    public ArrayList<Theater> eticketingTheaters;
    ArrayList<Header> headerList;
    //Interface for location
//    MoviesInterface moviesInterface;
    //Variables
    String url;
    Context myContext;
    double furthest;
    public static FragmentManager manager;
    public Realm tRealm;
    Header header = new Header();
    Header eHEader = new Header();


    //    public static Realm tRealm;
    final static byte DEFAULT_ZOOM_LEVEL = 10;
    public static final int LOCATION_PERMISSIONS = 99;
//    public boolean expanded;
//    private HashMap<LatLng, Theater> mapData;
//    private HashMap<String, Theater> markerTheaterMap;
//    Context myContext;
//    Activity myActivity;
//    private TheatersAdapter theatersAdapter;
//    EticketTheatersAdapter eticketTheatersAdapter;
//    GoogleMap googleMap;
//    MapView mMapView;
//    String url;
//    MaterialSearchBar searchGP;
//    Button searchThisArea;
//    RelativeLayout listViewMaps, mRelativeLayout;
//    RelativeLayout goneList;
//    ImageView mSearchClose, myloc, upArrow, downArrow;
//    View mProgress;
//    TextView listViewText, mapViewText;
//    ClusterManager<TheaterPin> theaterClusterManager;
//    RecyclerView theatersRecyclerView;
//    RecyclerView eTicketTheatersRecyclerView;
//    String TAG = "TAG";
//    LinkedList<Theater> nearbyTheaters;
//    SlidingUpPanelLayout theatersListView;
//    double furthest, LAT, LON;
//    View customInfoWindow;
//    ArrayList<Theater> eticketingTheaters;
//    ArrayList<Header> headerList;
//    Header header = new Header();
//    Header eHEader = new Header();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_theaters, container, false);
        ButterKnife.bind(this, rootView);

        rootView = inflater.inflate(R.layout.fr_theaters, container, false);


        /* Set up RecyclerView */

        LinearLayoutManager manager = new LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false);
        theatersRecyclerView = rootView.findViewById(R.id.theatersRecyclerView);
        theatersRecyclerView.setLayoutManager(manager);

        LinearLayoutManager m = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        eTicketTheatersRecyclerView = rootView.findViewById(R.id.eTicketTheatersRecyclerView);
        eTicketTheatersRecyclerView.setLayoutManager(m);

        searchThisArea = rootView.findViewById(R.id.SearchThisArea);
        listViewText = rootView.findViewById(R.id.ListViewText);
        upArrow = rootView.findViewById(R.id.uparrow);
        downArrow = rootView.findViewById(R.id.downarrow);
        mapViewText = rootView.findViewById(R.id.mapviewtext);
        headerList = new ArrayList<>();


        url = "http://moviepass.com/go/theaters";
        if (GoWatchItSingleton.getInstance().getCampaign() != null && !GoWatchItSingleton.getInstance().getCampaign().equalsIgnoreCase("no_campaign"))
            url = url + "/" + GoWatchItSingleton.getInstance().getCampaign();


        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchIcon = view.findViewById(R.id.search_inactive);
        searchLocation = view.findViewById(R.id.SearchGP);
        progressLoader = view.findViewById(R.id.progress);
        listViewMaps = view.findViewById(R.id.ListViewMaps);
        theatersListView = view.findViewById(R.id.sliding_layout);
        goneList = view.findViewById(R.id.goneList);
        listViewText = view.findViewById(R.id.ListViewText);
        mapViewText = view.findViewById(R.id.mapviewtext);
        upArrow = view.findViewById(R.id.uparrow);
        downArrow = view.findViewById(R.id.downarrow);
        customInfoWindow = View.inflate(getActivity(), R.layout.fr_theaters_infowindow, null);
        mapView = view.findViewById(R.id.MPMAPVIEW);
        myCurrentLocationButton = view.findViewById(R.id.myloc);
        searchThisArea = view.findViewById(R.id.SearchThisArea);
        zoomOnCurrentLocationButton = view.findViewById(R.id.myLocationButton);
        mapView.getMapAsync(this);


        manager = getChildFragmentManager();

        mapData = new HashMap<>();
        markerTheaterMap = new HashMap<>();
        headerList = new ArrayList<>();

        nearbyTheaters = new LinkedList<>();
        eticketingTheaters = new ArrayList<>();
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        if (BuildConfig.DEFAULT_LOCATION != null) {
            searchLocation.setText(BuildConfig.DEFAULT_LOCATION);
        }
        zoomOnCurrentLocationButton.setOnClickListener(v -> {
            getMyLocation();
        });

        searchIcon.setOnClickListener(view1 -> {

            searchLocation.enableSearch();
            searchLocation.setMaxSuggestionCount(0);
            fadeIn(searchLocation);
            searchLocation.setVisibility(View.VISIBLE);
            fadeOut(searchIcon);
            searchIcon.setVisibility(View.INVISIBLE);
            searchLocation.animate().start();
            searchLocation.setOnSearchActionListener(new SimpleOnSearchActionListener() {
                @Override
                public void onSearchConfirmed(CharSequence text) {
                    super.onSearchConfirmed(text);
                    searchMap(text.toString());
                    fadeOut(searchLocation);
                    searchLocation.setVisibility(View.GONE);
                    fadeIn(searchIcon);
                    searchIcon.setVisibility(View.VISIBLE);
                }
            });
        });

        //buildLocationSettingsRequest();
//        LogUtils.newLog(TAG, "onViewCreated: " + theatersListView.getPanelState());


        theatersListView.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (theatersListView.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    fadeIn(downArrow);
                    downArrow.setVisibility(View.VISIBLE);
                    fadeIn(mapViewText);
                    mapViewText.setVisibility(View.VISIBLE);

                    fadeOut(listViewText);
                    listViewText.setVisibility(View.INVISIBLE);
                    fadeOut(upArrow);
                    upArrow.setVisibility(View.INVISIBLE);
                    String url = "https://www.moviepass.com/go/list";
                    GoWatchItSingleton.getInstance().userOpenedTheaterTab(url, "list_view_click");

//                    String url = "http://moviepass.com/go/list";
//                    GoWatchItSingleton.getInstance().userOpenedTheaterTab(url, "list_view_click");

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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.setMinZoomPreference(1);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.

            this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(myContext, R.raw.map_style_json));
            this.googleMap.getUiSettings().setMapToolbarEnabled(false);
            this.googleMap.getUiSettings().setCompassEnabled(false);
            theaterClusterManager = new ClusterManager<>(myContext, this.googleMap);
            theaterClusterManager.setRenderer(new TheaterPinRenderer());
            this.googleMap.setOnMarkerClickListener(theaterClusterManager);


            theaterClusterManager.setOnClusterClickListener(this);
            theaterClusterManager.cluster();
        } catch (Resources.NotFoundException e) {
            LogUtils.newLog("MapsActivityRaw", "Can't find style.");
        }

        tRealm = Realm.getDefaultInstance();

        if (tRealm.isEmpty()) {
            getAllTheatersForStorage();
        } else {
            locationUpdateRealm();
        }
        progressLoader.setVisibility(View.VISIBLE);
        customInfoWindow = View.inflate(myContext, R.layout.fr_theaters_infowindow, null);
        this.googleMap.setOnMarkerClickListener(marker -> {
            ImageView etickIcon = customInfoWindow.findViewById(R.id.info_Etix);
            ImageView seatIcon = customInfoWindow.findViewById(R.id.info_Seat);
            etickIcon.setVisibility(View.GONE);
            seatIcon.setVisibility(View.GONE);
            this.googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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
                this.googleMap.animateCamera(cameraUpdate);
                marker.showInfoWindow();
            }
            return true;
        });

        this.googleMap.setOnInfoWindowClickListener(marker -> {
            showFragment(TheaterFragment.newInstance(markerTheaterMap.get(marker.getId())));
        });

    }

    static String[] permissions = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    @Nullable
    private Disposable locationSub;

    private boolean hasPermissions() {
        return checkSelfPermission(myContext, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED && checkSelfPermission(myContext, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    void onLocation(@NonNull UserLocation userLocation) {
        googleMap.setMyLocationEnabled(true);
        queryRealmLoadTheaters(userLocation.getLat(), userLocation.getLon());
        LatLng coordinates = new LatLng(userLocation.getLat(), userLocation.getLon());
        CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);
        googleMap.moveCamera(current);
    }

    void locationUpdateRealm() {
        if (!hasPermissions()) {
            requestPermissions(permissions, LOCATION_PERMISSIONS);
        } else {
            if (locationSub != null) {
                locationSub.dispose();
            }
            locationSub = locationManager.location()
                    .compose(Schedulers.Companion.singleDefault())
                    .subscribe(location -> {
                        onLocation(location);
                    }, error -> {

                    });
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        locationUpdateRealm();
    }

    private void getMyLocation() {
        if (!hasPermissions()) {
            requestPermissions(permissions, LOCATION_PERMISSIONS);
            return;
        }
        if (!locationManager.isLocationEnabled()) {
            new EnableLocation().show(getChildFragmentManager(), "fr_enablelocation");
            return;
        }
        progressLoader.setVisibility(View.VISIBLE);
        UserLocation last = locationManager.lastLocation();
        if (last != null) {
            queryRealmLoadTheaters(last.getLat(), last.getLon());
        } else {
            queryRealmLoadTheaters(0, 0);

        }
        theatersRecyclerView.getRecycledViewPool().clear();
        theatersAdapter.notifyDataSetChanged();
        if (searchThisArea.getVisibility() == View.VISIBLE) {
            searchThisArea.setVisibility(View.GONE);
            fadeOut(searchThisArea);
        }
    }

    @Override
    public void onTheaterClick(int pos, @NotNull Theater theater) {
        Log.d(Constants.TAG, "onTheaterClick: " + theater.getName());
        showFragment(TheaterFragment.newInstance(theater));
    }

    private class TheaterPinRenderer extends DefaultClusterRenderer<TheaterPin> {
        private final IconGenerator mClusterIconGenerator;

        public TheaterPinRenderer() {
            super(myContext, googleMap, theaterClusterManager);
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
                mClusterIconGenerator.setBackground(ContextCompat.getDrawable(myContext, R.drawable.icon_clustered_theater_pin));
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
            return false;
        }
    }

    @Override
    public boolean onClusterClick(final Cluster<TheaterPin> cluster) {
        return false;
    }


    public void queryRealmLoadTheaters(double newLat, double newLong) {
        Location userCurrentLocation;
        Location localPoints, smallLocal;

        theaterClusterManager.clearItems();
        nearbyTheaters.clear();
        eticketingTheaters.clear();

        userCurrentLocation = new Location(LocationManager.GPS_PROVIDER);
        userCurrentLocation.setLatitude(newLat);
        userCurrentLocation.setLongitude(newLong);

        RealmResults<Theater> allTheaters = tRealm.where(Theater.class).findAll();
        DecimalFormat df = new DecimalFormat("#.#");

        for (int K = 0; K < allTheaters.size(); K++) {
            Location pointB = new Location(LocationManager.GPS_PROVIDER);

            pointB.setLatitude(Objects.requireNonNull(allTheaters.get(K)).getLat());
            pointB.setLongitude(Objects.requireNonNull(allTheaters.get(K)).getLon());

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
            nearbyTheaters.get(j).setDistance(Double.parseDouble(df.format(mtrMLE).replaceAll(",", ".")));
            tRealm.commitTransaction();
        }
        //Sort through shorter list..
        Collections.sort(nearbyTheaters, (o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));
        if (nearbyTheaters.size() > 40) {
            nearbyTheaters.subList(40, nearbyTheaters.size()).clear();
        }

        for (int i = 0; i < nearbyTheaters.size() - 1; i++) {
            smallLocal = new Location(LocationManager.GPS_PROVIDER);
            smallLocal.setLatitude(nearbyTheaters.get(i).getLat());
            smallLocal.setLongitude(nearbyTheaters.get(i).getLon());

            furthest = userCurrentLocation.distanceTo(smallLocal);
            Theater etixSelect = nearbyTheaters.get(i);
            Collections.sort(eticketingTheaters, (o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));


            if (etixSelect.ticketTypeIsETicket() || etixSelect.ticketTypeIsSelectSeating()) {
                nearbyTheaters.remove(etixSelect);
                eticketingTheaters.add(etixSelect);
            }


            googleMap.setOnCameraMoveListener(() -> {
                Location cameraLocal = new Location(LocationManager.GPS_PROVIDER);
                cameraLocal.setLatitude(googleMap.getCameraPosition().target.latitude);
                cameraLocal.setLongitude(googleMap.getCameraPosition().target.longitude);


                double distance = userCurrentLocation.distanceTo(cameraLocal);
                double myLocationToCameraLocation = (distance / 1609.344);
                double myLocationToFurthestTheaterLocation = (furthest / 1609.344);

                if (myLocationToCameraLocation > myLocationToFurthestTheaterLocation) {
                    fadeIn(searchThisArea);
                    searchThisArea.setVisibility(View.VISIBLE);
                    searchThisArea.setOnClickListener(v -> {
                        eticketTheatersAdapter.notifyDataSetChanged();
                        double searchLat = googleMap.getCameraPosition().target.latitude;
                        double searchLon = googleMap.getCameraPosition().target.longitude;
                        queryRealmLoadTheaters(searchLat, searchLon);
                        searchThisArea.setVisibility(View.GONE);
                        fadeOut(searchThisArea);
                    });
                }
            });

        }

        displayTheatersFromRealm(nearbyTheaters);
    }

    public void displayTheatersFromRealm(LinkedList<Theater> theatersList) {
        for (Theater theater : theatersList) {
            LatLng location = new LatLng(theater.getLat(), theater.getLon());
            mapData.put(location, theater);
            final int position;
            position = theatersList.indexOf(theater);
            theaterClusterManager.addItem(new TheaterPin(theater.getLat(), theater.getLon(), theater.getName(), R.drawable.theaterpinstandard, position, theater));
            theaterClusterManager.cluster();

            final CameraPosition[] mPreviousCameraPosition = {null};
            googleMap.setOnCameraIdleListener(() -> {
                CameraPosition position1 = googleMap.getCameraPosition();
                if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position1.zoom) {
                    mPreviousCameraPosition[0] = googleMap.getCameraPosition();
                }
            });

        }


        if (theatersList.size() == 0 && eticketingTheaters.size() == 0) {
            theatersListView.setEnabled(false);
            listViewText.setTextColor(getResources().getColor(R.color.gray_icon));
            upArrow.setColorFilter(getResources().getColor(R.color.gray_icon));
            Toast.makeText(getActivity(), "No TheatersFragment found", Toast.LENGTH_SHORT).show();
        } else {
            theatersListView.setEnabled(true);
            listViewText.setTextColor(getResources().getColor(R.color.white));
            upArrow.setColorFilter(getResources().getColor(R.color.white));
        }

        if (eticketingTheaters.size() > 0) {
            for (Theater eTheater : eticketingTheaters) {
                LatLng location = new LatLng(eTheater.getLat(), eTheater.getLon());
                mapData.put(location, eTheater);
                final int position;
                position = theatersList.indexOf(eTheater);
                theaterClusterManager.addItem(new TheaterPin(eTheater.getLat(), eTheater.getLon(), eTheater.getName(), R.drawable.theaterpinstandard, position, eTheater));
                theaterClusterManager.cluster();
            }
            eTicketTheatersRecyclerView.setVisibility(View.VISIBLE);
            eHEader.setEticket("E-Ticketing");
            eticketTheatersAdapter = new EticketTheatersAdapter(eHEader, this, eticketingTheaters);
            eTicketTheatersRecyclerView.setAdapter(eticketTheatersAdapter);
            eticketTheatersAdapter.notifyDataSetChanged();
        } else {
            eHEader.setEticket("");
        }

        theatersAdapter = new TheatersAdapter(header, this, theatersList);
        theatersRecyclerView.setAdapter(theatersAdapter);
        theatersAdapter.notifyDataSetChanged();
    }


    void searchMap(String searchString) {
        Geocoder geo = new Geocoder(myContext);
        try {
            List<Address> addresses = geo.getFromLocationName(searchString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                GoWatchItSingleton.getInstance().searchEvent(address.toString(), "theatrical_search", url);
                queryRealmLoadTheaters(address.getLatitude(), address.getLongitude());
                theatersAdapter.notifyDataSetChanged();
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
                googleMap.animateCamera(cameraUpdate);
            } else {
                RealmResults<Theater> searchArea = tRealm.where(Theater.class)
                        .contains("city", searchString)
                        .findAll();
                GoWatchItSingleton.getInstance().searchEvent(searchString, "theatrical_search", url);

                for (int i = 0; i < searchArea.size(); i++) {
                    LAT = searchArea.get(i).getLat();
                    LON = searchArea.get(i).getLon();

                }
                if (LAT != 0.0 && LON != 0.0) {
                    queryRealmLoadTheaters(LAT, LON);
                    theatersAdapter.notifyDataSetChanged();
                    LatLng latLng = new LatLng(LAT, LON);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
                    googleMap.animateCamera(cameraUpdate);
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
        AndroidSupportInjection.inject(this);
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
                        LogUtils.newLog(Constants.TAG, "onSuccess: ");
                        locationUpdateRealm();
                    }, error -> {
                        // Transaction failed and was automatically canceled.
                        LogUtils.newLog(Constants.TAG, "Realm onError: " + error.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Call<LocalStorageTheaters> call, Throwable t) {
                Toast.makeText(myContext, "Error while downloading Theaters.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}