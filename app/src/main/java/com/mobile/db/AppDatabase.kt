package com.mobile.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.mobile.history.model.ReservationHistory
import com.mobile.model.Theater
import com.mobile.theater.TheaterDao

@Database(entities = [Theater::class, ReservationHistory::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

   abstract fun theaterDao(): TheaterDao
   abstract fun reservationDao(): ReservationDao
}