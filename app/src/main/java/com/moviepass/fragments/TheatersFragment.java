package com.moviepass.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lapism.searchview.SearchView;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.TheatersClickListener;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.activities.TheaterActivity;
import com.moviepass.adapters.TheatersAdapter;
import com.moviepass.model.Theater;
import com.moviepass.model.TheatersResponse;
import com.moviepass.network.RestClient;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by anubis on 6/6/17.
 */

public class TheatersFragment extends Fragment implements OnMapReadyCallback, TheatersClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static String LOCATION_PERMISSIONS[] = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private final static int REQUEST_LOCATION_CODE = 0;
    final static byte DEFAULT_ZOOM_LEVEL = 12;
    int SUPPORT_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;


    private HashMap<LatLng, Theater> mMapData;

    private GoogleApiClient mGoogleApiClient;
    boolean mLocationAcquired;
    private Location mMyLocation;
    private TheatersAdapter mTheatersAdapter;

    GoogleMap mMap;
    MapView mMapView;
    private Marker selectedMarker;

    LocationUpdateBroadCast mLocationBroadCast;
    TheatersClickListener mTheatersClickListener;
    private OnFragmentInteractionListener listener;
    SupportPlaceAutocompleteFragment places;

    SearchView mSearchLocation;
    ImageView mSearchClose;
    CardView mCardView;

    LayoutAnimationController controller;

    ArrayList<Theater> mTheaters;
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

        selectedMarker = null;

        //Start location tasks
        UserLocationManagerFused.getLocationInstance(getActivity()).startLocationUpdates();

        mLocationBroadCast = new LocationUpdateBroadCast();
        getActivity().registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));

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

        SupportPlaceAutocompleteFragment places = (SupportPlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setFilter(typeFilter);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
//                closeKeyboard();

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
        if (mMap != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_CODE);
                } else {
                    currentLocationTasks();
                }
            } else {
                currentLocationTasks();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
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

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            getActivity().registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }

        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            getActivity().registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            getActivity().unregisterReceiver(mLocationBroadCast);
        } catch (IllegalArgumentException is) {
            is.printStackTrace();
        }
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

    class LocationUpdateBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mLocationBroadCast != null) {
                    getActivity().unregisterReceiver(mLocationBroadCast);
                }
            } catch (IllegalArgumentException is) {
                is.printStackTrace();
            }
            UserLocationManagerFused.getLocationInstance(getActivity()).stopLocationUpdates();
            onLocationChanged(UserLocationManagerFused.getLocationInstance(context).mCurrentLocation);
        }
    }

    protected void onLocationChanged(Location location) {
        UserLocationManagerFused.getLocationInstance(getActivity()).stopLocationUpdates();

        if (location != null) {
            UserLocationManagerFused.getLocationInstance(getActivity()).updateLocation(location);

            mMyLocation = location;

            LatLng coordinates = new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude());
            CameraUpdate current = CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL);

            mMap.moveCamera(current);
            mMap.clear();

            loadTheaters(mMyLocation.getLatitude(), mMyLocation.getLongitude());

            mLocationAcquired = true;
        }
    }

    public void currentLocationTasks() {
        getActivity().registerReceiver(mLocationBroadCast, new IntentFilter(Constants.LOCATION_UPDATE_INTENT_FILTER));
        UserLocationManagerFused.getLocationInstance(getActivity()).startLocationUpdates();
        mLocationAcquired = false;

        boolean enabled = UserLocationManagerFused.getLocationInstance(getActivity()).isLocationEnabled();
        if (!enabled) {
//            showDialogGPS();
        } else {
            Location location = UserLocationManagerFused.getLocationInstance(getActivity()).mCurrentLocation;
            onLocationChanged(location);

            if (location != null) {
                UserLocationManagerFused.getLocationInstance(getActivity()).requestLocationForCoords(location.getLatitude(), location.getLongitude(), getActivity());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_CODE) {

            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getActivity(), "You must grant permission to use MoviePass.", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void loadTheaters(Double latitude, Double longitude) {
        RestClient.getAuthenticated().getTheaters(latitude, longitude)
                .enqueue(new Callback<TheatersResponse>() {
                    @Override
                    public void onResponse(Call<TheatersResponse> call, final Response<TheatersResponse> response) {
                        TheatersResponse theaters = response.body();
                        if (theaters != null) {
                            final List<Theater> theaterList = theaters.getTheaters();

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
                                    }

                                    MarkerOptions options = new MarkerOptions()
                                            .title(theater.getName())
                                            .snippet(theater.getAddress())
                                            .position(location)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin_unselected));

                                    mMap.addMarker(options);

                                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            final Marker finalMarker = marker;

                                            mRecyclerView.animate()
                                                    .translationY(mRecyclerView.getHeight())
                                                    .alpha(0.5f)
                                                    .setListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            super.onAnimationEnd(animation);

                                                            //Load New RecyclerView Based on User's Click
                                                            reloadTheaters(finalMarker.getPosition().latitude, finalMarker.getPosition().longitude);
                                                        }
                                                    });

                                            return false;
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

    private void reloadTheaters(Double latitude, final Double longitude) {
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

                                    if (position == 0) {
                                        MarkerOptions options = new MarkerOptions()
                                                .title(theater.getName())
                                                .snippet(theater.getAddress())
                                                .position(location)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin));

                                        mMap.addMarker(options);
                                    } else {
                                        MarkerOptions options = new MarkerOptions()
                                                .title(theater.getName())
                                                .snippet(theater.getAddress())
                                                .position(location)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.theater_pin_unselected));

                                        mMap.addMarker(options);
                                    }

                                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            final Marker finalMarker = marker;

                                            mRecyclerView.animate()
                                                    .translationY(mRecyclerView.getHeight())
                                                    .alpha(0.5f)
                                                    .setListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            super.onAnimationEnd(animation);

                                                            //Load New RecyclerView Based on User's Click
                                                            reloadTheaters(finalMarker.getPosition().latitude, finalMarker.getPosition().longitude);
                                                        }
                                                    });

                                            return false;
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

//        closeKeyboard();

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

    public void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}