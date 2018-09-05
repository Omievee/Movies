package com.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobile.activities.LogInActivity
import com.mobile.home.HomeActivity
import com.mobile.model.Movie
import com.mobile.model.Theater
import com.mobile.utils.startIntentIfResolves
import io.realm.Realm
import io.realm.RealmConfiguration
import org.parceler.Parcels


class BroadcastReceiver : BroadcastReceiver() {

    var context: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context ?: return

        intent?.extras?.let {
            val url = it.get("uri")?.toString()?.trim()
            determineCategory(url)
        }
    }

    private fun determineCategory(url: String?) {
        val type = url?.split("/".toRegex())
        when (type?.get(4)) {
            "movies" -> {
                val movieID = type[5]
                findCorrespondingMovie(Integer.valueOf(movieID))
            }
            "theaters" -> {
                val theaterID = type[5]
                findCorrespondingTheater(Integer.valueOf(theaterID))
            }
        }
    }

    private fun findCorrespondingTheater(theaterId: Int) {
        //TODO : Same for theaters...
    }

    private fun findCorrespondingMovie(movieId: Int) {
        val config = RealmConfiguration.Builder()
                .name("Movies.Realm")
                .deleteRealmIfMigrationNeeded()
                .build()

        val moviesRealm = Realm.getInstance(config)

        val movie = moviesRealm.copyFromRealm<Movie>(moviesRealm.where(Movie::class.java)
                .equalTo("id", movieId)
                .findAll())
        if (movie != null && movie.size > 0) {
            objectFromRealmQuery(null, movie[0])
        }

    }

    private fun objectFromRealmQuery(theaterObject: Theater? = null, movie: Movie? = null) {
        when (UserPreferences.userId) {
            0 -> {
                if (movie != null) {
                    val receivedIntent = Intent(context, LogInActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, Parcels.wrap(movie))
                    context.startIntentIfResolves(receivedIntent)
                } else {
                    val receivedIntent = Intent(context, LogInActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, Parcels.wrap(theaterObject))
                    context.startIntentIfResolves(receivedIntent)
                }
            }
            else -> {
                if (movie != null) {
                    val receivedIntent = Intent(context, HomeActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, Parcels.wrap(movie))
                    context.startIntentIfResolves(receivedIntent)
                } else {
                    val receivedIntent = Intent(context, HomeActivity::class.java)
                    receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, Parcels.wrap(theaterObject))
                    context.startIntentIfResolves(receivedIntent)
                }
            }
        }


    }


}


