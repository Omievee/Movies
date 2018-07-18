package com.mobile.theater

import android.content.Context
import android.os.Bundle
import android.support.transition.Transition
import android.support.transition.TransitionListenerAdapter
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.*
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.mobile.UserPreferences
import com.mobile.analytics.AnalyticsManager
import com.mobile.fragments.MPFragment
import com.mobile.fragments.ScreeningsData
import com.mobile.fragments.ScreeningsFragment
import com.mobile.keyboard.KeyboardManager
import com.mobile.location.BoundingBox
import com.mobile.location.Geocoder
import com.mobile.location.UserAddress
import com.mobile.location.UserLocation
import com.mobile.model.Theater
import com.moviepass.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_theater_map.*
import javax.inject.Inject

const val MAP_VIEW_BUNDLE = "MapViewBundleKey"

fun UserLocation.toLatLng(): LatLng {
    return LatLng(lat, lon)
}

fun LatLng.toUserLocation(): UserLocation {
    return UserLocation(latitude, longitude)
}

fun List<Theater>.centerPoint(): UserLocation? {
    if (isEmpty()) {
        return null
    }
    if (size == 1) {
        return UserLocation(first().lat, first().lon)
    }
    val a = get(0)
    val b = get(1)
    val bounds = LatLngBounds.Builder().include(LatLng(a.lat, a.lon)).include(LatLng(b.lat, b.lon)).build()
    forEach {
        bounds.including(LatLng(it.lat, it.lon))
    }
    return bounds.center.toUserLocation()
}

fun LatLngBounds.toBox(): BoundingBox {
    return BoundingBox(
            center.toUserLocation(), UserLocation.haversine(
            lat1 = southwest.latitude,
            lon1 = southwest.longitude,
            lat2 = northeast.latitude,
            lon2 = northeast.longitude
    )
    )
}

val LatLngBounds.radius: Double
    get() {
        return UserLocation.haversine(
                lat1 = southwest.latitude,
                lon1 = southwest.longitude,
                lat2 = northeast.latitude,
                lon2 = northeast.longitude)
    }

class TheaterMapFragment : MPFragment(), OnMapReadyCallback {

    var map: GoogleMap? = null
    var clusterManager: ClusterManager<TheaterPin>? = null
    var renderer: TheaterPinRenderer? = null
    var locationSub: Disposable? = null
    var theaterSub: Disposable? = null
    var geocodeSub: Disposable? = null
    var searchSub: Disposable? = null
    var listSearchSub:Disposable? = null
    var theatersFetchedInitially = false

    @Inject
    lateinit var theaterManager: TheaterManager

    @Inject
    lateinit var geocoder: Geocoder

    @Inject
    lateinit var theaterUIManager: TheaterUIManager

    @Inject
    lateinit var keyboardManager: KeyboardManager

    @Inject
    lateinit var analyticsManager:AnalyticsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_theater_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState?.getBundle(MAP_VIEW_BUNDLE))
        mapView.getMapAsync(this)
        mapSearchBox.listener = object : MapSearchBoxListener {
            override fun onClose() {
                hideSearchBox()
            }

            override fun onSearch(query: String) {
                geocodeSub?.dispose()
                geocodeSub = geocoder.reverseLocation(query)
                        .subscribe({
                            search(it)
                        }, {

                        })
                analyticsManager.onTheaterSearch(query)
            }

        }
        searchIconCard.setOnClickListener {
            showSearchBox()
        }
        listCard.setOnClickListener {
            activity?.onBackPressed()
        }
        subscribeToSearch()
    }

    private fun subscribeToSearch() {
        listSearchSub?.dispose()
        listSearchSub = theaterUIManager
                .listTheaters()
                .subscribe {  }
    }

    private fun panCameraTo(it: TheatersPayload) {
        var zoom = map?.cameraPosition?.zoom?:10f
        if(it.theaters.isEmpty()) {
            zoom = zoom.minus(2)
        }
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.location.toLatLng(), zoom))
        keyboardManager.hide()
    }

    private fun search(queryAddress: UserAddress) {
        searchSub?.dispose()
        searchSub = theaterManager.search(queryAddress)
                .map {
                    val latLngBounds = it.centerPoint()

                    val location = queryAddress.location

                    val centerPoint = when (latLngBounds) {
                        null -> {
                            location
                        }
                        else -> latLngBounds
                    }
                    val payload = TheatersPayload(centerPoint, it)
                    theaterUIManager.mappedTheaters(payload)
                    payload
                }
                .subscribe({ theaters ->
                    panCameraTo(theaters)
                }, {})
    }

    private fun hideSearchBox() {
        val set = TransitionSet()
        set.duration = 100
        val listener = object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                keyboardManager.hide()
            }
        }
        set.addListener(listener)
        TransitionManager.beginDelayedTransition(theaterMapCL, set)
        mapSearchBoxCard.visibility = View.INVISIBLE
        searchIconCard.visibility = View.VISIBLE
    }

    private fun showSearchBox() {
        TransitionManager.beginDelayedTransition(theaterMapCL)
        mapSearchBoxCard.visibility = View.VISIBLE
        searchIconCard.visibility = View.INVISIBLE
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        theaterSub?.dispose()
        locationSub?.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var bundle = outState.getBundle(MAP_VIEW_BUNDLE)
        when (bundle == null) {
            true -> {
                bundle = Bundle()
                outState.putBundle(MAP_VIEW_BUNDLE, bundle)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onBack(): Boolean {
        return when (mapSearchBoxCard.visibility) {
            View.VISIBLE -> {
                mapSearchBoxCard.visibility = View.INVISIBLE
                searchIconCard.visibility = View.VISIBLE
                true
            }
            else -> false
        }
    }

    override fun onMapReady(map: GoogleMap) {
        val context = context ?: return
        this.map = map
        map.apply {
            uiSettings.isMyLocationButtonEnabled = true
            setMinZoomPreference(1f)
            setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_json))
            uiSettings.isCompassEnabled = false
            setOnMarkerClickListener { marker ->
                displayMarker(marker)
            }
            setOnCameraMoveListener {
                fetchTheaters()
            }
            setOnInfoWindowClickListener {
                val theater = renderer?.markerToTheater(it)
                showFragment(ScreeningsFragment.newInstance(ScreeningsData(theater = theater)))
            }
            setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoContents(p0: Marker): View? {
                    return null
                }

                override fun getInfoWindow(p0: Marker): View? {
                    return CustomInfoWindow(context).apply {
                        val theater = renderer?.markerToTheater(p0)
                        measure(makeMeasureSpec(
                                resources.displayMetrics.widthPixels, UNSPECIFIED
                        ), makeMeasureSpec(resources.displayMetrics.heightPixels, UNSPECIFIED))
                        val width = measuredWidth
                        val height = measuredHeight
                        bind(p0, theater!!)
                        setOnClickListener {
                            showFragment(ScreeningsFragment.newInstance(ScreeningsData(theater = theater)))
                        }
                    }
                }


            })
        }
        clusterManager = ClusterManager(context, map)
        renderer = TheaterPinRenderer(context, map, clusterManager)
        clusterManager?.renderer = renderer
        clusterManager?.setOnClusterClickListener { _ ->
            false
        }
        clusterManager?.cluster()
        val location = theaterUIManager.listTheatersLocation()
        when(location) {
            null-> fetchUsersLocation()
            else-> moveAndFetch(location)
        }
    }

    private fun fetchTheaters() {
        val projection = map?.projection ?: return
        val visibleRegion = projection.visibleRegion ?: return
        fetchTheatersForLocation(visibleRegion.latLngBounds.center.toUserLocation(), visibleRegion.latLngBounds.toBox())
    }

    private fun fetchUsersLocation() {
        locationSub?.dispose()
        locationSub = theaterManager
                .theaterLocation()
                .subscribe({
                    moveAndFetch(it)
                }, {

                })
    }

    private fun moveAndFetch(it:UserLocation) {
        moveCamera(it)
        fetchTheaters()
    }

    private fun moveCamera(it: UserLocation) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 10f))
    }

    private fun fetchTheatersForLocation(userLocation: UserLocation, box: BoundingBox) {
        theaterSub?.dispose()
        theaterSub = theaterManager
                .theaters(userLocation, box)
                .map {
                    val location = it.centerPoint() ?: userLocation
                    theaterUIManager.mappedTheaters(TheatersPayload(location, it))
                    it
                }
                .subscribe({
                    displayTheaters(it)
                }, {})
    }

    private fun displayTheaters(it: List<Theater>) {
        val bounds = map?.projection?.visibleRegion?.latLngBounds ?: return
        val manager = clusterManager ?: return
        manager.clearItems()
        it.forEach {
            val pin = TheaterPin(it)
            when (bounds.contains(pin.latLng)) {
                true -> manager.addItem(TheaterPin(it))
            }
        }
        manager.cluster()
    }

    private fun displayMarker(marker: Marker): Boolean {
        if (marker.title != null) {
            marker.showInfoWindow()
        }
        return true
    }
}