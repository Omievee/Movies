package com.mobile.theater

import android.arch.persistence.room.*
import com.mobile.model.Theater

@Dao
interface TheaterDao {

    @Query("select * from Theater where id=:id")
    fun loadTheaterById(id:Int):Theater

    @Query("select * from Theater")
    fun findAll():List<Theater>

    @Query("select * from Theater where lat>:southWestLat and lat<:northEastLat and lon>:southWestLon and lon<:northEastLon")
    fun findAll(southWestLat:Double, northEastLat:Double, southWestLon:Double, northEastLon:Double):List<Theater>

    @Query("delete from Theater")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(theaters:List<Theater>)

    @Transaction
    fun replaceTheaters(theaters:List<Theater>) {
        deleteAll()
        saveAll(theaters)
    }
}
