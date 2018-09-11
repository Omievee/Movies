package com.mobile.di

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import com.mobile.UserPreferences
import com.mobile.application.Application
import com.mobile.db.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideRealmTheater(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java,"local_storage.db")
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        UserPreferences.theatersLoadedToday = false
                        UserPreferences.clearHistoryLoadedDate()
                    }
                })
                .build()
    }

}