package com.mobile.db

import android.arch.persistence.room.*
import com.amazonaws.services.s3.model.ExtraMaterialsDescription
import com.mobile.history.History
import com.mobile.history.model.ReservationHistory

@Dao
interface ReservationDao {

    @Query("select * from ReservationHistory")
    fun getHistory():List<ReservationHistory>

    @Query("delete from ReservationHistory")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(list:List<ReservationHistory>)

    @Transaction
    fun replaceHistory(list: List<ReservationHistory>) {
        deleteAll()
        saveAll(list)
    }

    @Update
    fun update(history: ReservationHistory)

}
