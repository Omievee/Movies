package com.mobile.theater

import com.mobile.UserPreferences
import com.mobile.location.BoundingBox
import com.mobile.location.LocationManager
import com.mobile.location.UserAddress
import com.mobile.location.UserLocation
import com.mobile.model.Theater
import com.mobile.network.StaticApi
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.realm.Case
import io.realm.Realm
import javax.inject.Provider

class TheaterManagerImpl(val api: StaticApi, @TheaterScope val realm: Provider<Realm>, val locationManager: LocationManager) : TheaterManager {

    private val userDefinedLocation: PublishSubject<UserLocation> = PublishSubject.create()
    private var disposable: Disposable? = null
    private var locationSub: Disposable? = null

    init {
        val loc = UserPreferences.userLocation ?: locationManager.lastLocation()
        when (loc) {
            null -> {
                fetchLocation()
            }
            else -> {
                userDefinedLocation.onNext(loc)
            }
        }
    }

    private fun fetchLocation() {
        locationSub?.dispose()
        locationSub = locationManager
                .location()
                .subscribe({ v ->
                    userDefinedLocation.onNext(v)
                }, {

                })
    }

    override fun theaterLocation(): Observable<UserLocation> {
        val observ = userDefinedLocation.compose(Schedulers.observableDefault())
                .doOnSubscribe {
                    fetchLocation()
                }
        return observ
    }

    override fun theaters(userLocation: UserLocation?, box: BoundingBox?): Observable<List<Theater>> {
        val obs: Observable<List<Theater>> = Observable.create { emitter ->
            val theatersEverLoaded = UserPreferences.theatersLoadedToday
            val theaters = realm.get().copyFromRealm(queryRealm(userLocation, box))
            if (emitter.isDisposed) {
                return@create
            }
            when(theaters.isEmpty()) {
                false-> emitter.onNext(theaters)
                theatersEverLoaded->emitter.onNext(theaters)
            }
            if (!theatersEverLoaded) {
                getTheatersFromApi(userLocation, box, emitter)
            } else {
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }

        }
        return obs.compose(Schedulers.observableDefault())
    }

    override fun search(address:UserAddress): Single<List<Theater>> {
        val single:Single<List<Theater>> = Single.create { it->
            val theaters = queryRealm(address)
            if(it.isDisposed) {
                return@create
            }
            val theatersfound = realm.get().copyFromRealm(theaters)
            if(!it.isDisposed) {
                it.onSuccess(theatersfound)
            }
        }
        return single.compose(Schedulers.singleDefault())
    }

    private fun getTheatersFromApi(userLocation: UserLocation?, box:BoundingBox?, emitter: ObservableEmitter<List<Theater>>) {
        disposable?.dispose()
        disposable = api
                .getAllMoviePassTheaters()
                .map { reservationHistoryResponse ->
                    reservationHistoryResponse.theaters?.let {
                        realm.get().executeTransaction { transaction ->
                            transaction.insertOrUpdate(it)
                        }
                        UserPreferences.theatersLoadedToday = true
                        if (userLocation != null) {
                            it.sortAndFilter(userLocation, box)
                        } else {
                            it
                        }
                    } ?: throw RuntimeException()
                }
                .doAfterTerminate {
                    if (emitter.isDisposed) {
                        return@doAfterTerminate
                    }
                    emitter.onComplete()
                }
                .subscribe { success, errror ->
                    if (emitter.isDisposed) {
                        return@subscribe
                    }
                    errror?.let {
                        emitter.onError(it)
                    } ?: emitter.onNext(success)

                }
    }

    private fun queryRealm(str:UserAddress, radius:Double = 10.0): List<Theater> {
        val query = realm.get()
                .where(Theater::class.java)
        val box = BoundingBox(str.location,1_609.34*radius)
        query.greaterThan("lat", box.southWest.lat)
        query.greaterThan("lon", box.southWest.lon)
        query.lessThan("lat", box.northEast.lat)
        query.lessThan("lon", box.northEast.lon)
        val theaters = query.findAll().sortAndFilter(str.location,box).take(40)
        if(theaters.isEmpty() && radius < 40) {
            return queryRealm(str, radius+10)
        } else {
            return theaters
        }
    }

    private fun queryRealm(userLocation: UserLocation?, box:BoundingBox?): List<Theater> {
        val query = realm.get()
                .where(Theater::class.java)
        val loc = userLocation ?: return query.findAll()
        if(box!=null) {
            query.greaterThan("lat", box.southWest.lat)
            query.greaterThan("lon", box.southWest.lon)
            query.lessThan("lat", box.northEast.lat)
            query.lessThan("lon", box.northEast.lon)
        }
        return query.findAll().sortAndFilter(loc, box).take(40)
    }
}

fun List<Theater>.sortAndFilter(userLocation: UserLocation, box: BoundingBox?): List<Theater> {
    return sortedWith(compareBy(
            {
                UserLocation.haversine(it.lat, it.lon, userLocation.lat, userLocation.lon)
            })).take(40).sortedWith(compareBy({ !it.ticketTypeIsSelectSeating() }, { !it.ticketTypeIsETicket()
            })).toMutableList()
}