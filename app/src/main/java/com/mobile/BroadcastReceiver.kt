package com.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.appboy.push.AppboyNotificationUtils
import com.mobile.activities.LogInActivity
import com.mobile.home.HomeActivity
import com.mobile.model.Movie
import com.mobile.model.Theater
import com.mobile.utils.startIntentIfResolves
import io.realm.Realm
import io.realm.RealmConfiguration


class BroadcastReceiver : BroadcastReceiver() {

    lateinit var context: Context
    lateinit var intent: Intent
    lateinit var movieObject: Movie

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context ?: return
        this.intent = intent ?: return
        val url = intent.extras?.get("uri")?.toString()?.trim()
        determineCategory(url)
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
            movieObject = movie[0]
            userOpenedPush(null, movieObject, intent)
        }
    }


    fun userOpenedPush(theater: Theater? = null, movie: Movie? = null, intent: Intent) {
        val notificationOpenedAction = context.packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX

        if (notificationOpenedAction == intent.action) {
            when (UserPreferences.userId) {
                0 -> {
                    if (movie != null) {
                        val receivedIntent = Intent(context, LogInActivity::class.java)
                        receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, movieObject)
                        context.startIntentIfResolves(receivedIntent)
                    } else {
                        val receivedIntent = Intent(context, LogInActivity::class.java)
                        // receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, Parcels.wrap(theaterObject))
                        context.startIntentIfResolves(receivedIntent)
                    }
                }
                else -> {
                    if (movie != null) {
                        Log.d(">>>>>>>>>>><<<<<<<<<<<", "<<<<<<<<<<<ACTION>>>>>>>>> " + movieObject.title)
                        val receivedIntent = Intent(context, HomeActivity::class.java)
                        receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, movieObject)
                        context.startIntentIfResolves(receivedIntent)
                    } else {
                        val receivedIntent = Intent(context, HomeActivity::class.java)
                        //  receivedIntent.putExtra(Constants.APPBOY_DEEP_LINK_KEY, Parcels.wrap(theaterObject))
                        context.startIntentIfResolves(receivedIntent)
                    }
                }
            }


        }
    }
}


