package com.mobile.theater

import com.mobile.UserPreferences
import com.mobile.deeplinks.DeepLinkCategory
import com.mobile.location.BoundingBox
import com.mobile.location.LocationManager
import com.mobile.location.UserAddress
import com.mobile.location.UserLocation
import com.mobile.model.AmcDmaMap
import com.mobile.model.Theater
import com.mobile.network.StaticApi
import com.mobile.rx.Schedulers
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Provider

class TheaterManagerImpl(
        val api: StaticApi,
        val theaterDao: Provider<TheaterDao>,
        val locationManager: LocationManager,
        val amcDmaMap: AmcDmaMap
) : TheaterManager {

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
            val theaters = queryRealm(userLocation, box)
            if (emitter.isDisposed) {
                return@create
            }
            when (theaters.isEmpty()) {
                false -> emitter.onNext(theaters)
                theatersEverLoaded -> emitter.onNext(theaters)
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

    override fun search(address: UserAddress): Single<List<Theater>> {
        val single: Single<List<Theater>> = Single.create { it ->
            val theaters = queryRealm(address)
            if (it.isDisposed) {
                return@create
            }
            if (!it.isDisposed) {
                it.onSuccess(theaters)
            }
        }
        return single.compose(Schedulers.singleDefault())
    }


    private fun getTheatersFromApi(userLocation: UserLocation?, box: BoundingBox?, emitter: ObservableEmitter<List<Theater>>) {
        disposable?.dispose()
        disposable = api
                .getAllMoviePassTheaters()
                .compose(Schedulers.singleBackground())
                .map { reservationHistoryResponse ->
                    reservationHistoryResponse.theaters?.let {
                        theaterDao.get().saveAll(it)
                        UserPreferences.theatersLoadedToday = true
                        if (userLocation != null) {
                            it.sortAndFilter(userLocation, dmaMap = amcDmaMap)
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

    private fun queryRealm(str: UserAddress, radius: Double = 30.0): List<Theater> {
        val box = BoundingBox(str.location, 1_609.34 * radius)
        val theaters = theaterDao.get().findAll(box.southWest.lat, box.northEast.lat, box.southWest.lon, box.northEast.lon)
        if (theaters.isEmpty() && radius < 40) {
            return queryRealm(str, radius + 10)
        } else {
            return theaters.sortAndFilter(str.location,amcDmaMap).take(40)
        }
    }

    private fun queryRealm(userLocation: UserLocation?, boxArg: BoundingBox?): List<Theater> {
        val loc = userLocation ?: return theaterDao.get().findAll()
        val box = boxArg ?: BoundingBox(loc, 30.0 * 1_609.34)
        return theaterDao.get().findAll(box.southWest.lat, box.northEast.lat, box.southWest.lon, box.northEast.lon).sortAndFilter(loc, amcDmaMap).take(40)
    }

    override fun theaterDeepLink(theaterId: Int): Observable<DeepLinkCategory> {
        return theaters()
                .map { theaterList ->
                    theaterList.first { it.id == theaterId }
                }.map {
                    DeepLinkCategory(theater = it)
                }
    }
}

fun List<Theater>.sortAndFilter(userLocation: UserLocation, dmaMap: AmcDmaMap): List<Theater> {
    return sortedWith(compareBy {
        UserLocation.haversine(it.lat, it.lon, userLocation.lat, userLocation.lon)
    }).take(40)
            .sortedWith(compareBy({ !it.ticketTypeIsSelectSeating() }, {
                !it.ticketTypeIsETicket()
            },
                    {
                        dmaMap.shouldMoveToBottom(it)
                    }
            )).toMutableList()
}