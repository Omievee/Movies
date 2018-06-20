package com.mobile.theater

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.mobile.model.Theater
import com.moviepass.R

class TheaterPinRenderer(val context: Context, map: GoogleMap?, clusterManager: ClusterManager<TheaterPin>?) : DefaultClusterRenderer<TheaterPin>(context, map, clusterManager) {

    val iconGenerator = IconGenerator(context)

    val markerTheaterMap: MutableMap<String, Theater> = mutableMapOf()

    val eticketIcon = BitmapDescriptorFactory.fromResource(R.drawable.eticket_map_icon)
    val regularDrawable = ContextCompat.getDrawable(context, R.drawable.post_pin)!!
    val regularIcon = BitmapDescriptorFactory.fromResource(R.drawable.post_pin)
    val clusterIcon = ContextCompat.getDrawable(context, R.drawable.icon_clustered_theater_pin)


    override fun onBeforeClusterItemRendered(theaterPin: TheaterPin, markerOptions: MarkerOptions) {
        if (theaterPin.theater.ticketTypeIsSelectSeating() || theaterPin.theater.ticketTypeIsETicket()) {
            markerOptions.icon(eticketIcon).title(theaterPin.title).snippet(theaterPin.snippet)
        } else {
            markerOptions.icon(regularIcon).title(theaterPin.title).snippet(theaterPin.snippet)
        }
        //markerOptions.infoWindowAnchor(0f,-.25f)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<TheaterPin>, markerOptions: MarkerOptions) {
        try {
            iconGenerator.setBackground(clusterIcon)
            iconGenerator.setTextAppearance(R.style.ThemeOverlay_AppCompat_Dark)

            val icon = iconGenerator.makeIcon(cluster.size.toString())
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))

            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            val theaterPins = mutableListOf<Drawable>()
            for (p in cluster.items) {
                // Draw 4 at most.
                if (theaterPins.size == 0) break
                theaterPins.add(regularDrawable)
            }
        } catch (e: Exception) {
        }
    }

    fun markerToTheater(marker: Marker): Theater? {
        return markerTheaterMap[marker.id]
    }

    override fun onClusterItemRendered(theaterPin: TheaterPin, marker: Marker) {
        super.onClusterItemRendered(theaterPin, marker)
        val theater = theaterPin.theater
        markerTheaterMap[marker.id] = theater
    }

    override fun shouldRenderAsCluster(cluster: Cluster<TheaterPin>): Boolean {
        // Always render clusters.
        return false
    }
}