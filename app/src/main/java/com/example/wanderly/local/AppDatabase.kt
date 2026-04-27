package com.example.wanderly.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wanderly.local.places.PlacesDao
import com.example.wanderly.local.places.PlacesEntity

@Database(
    entities = [PlacesEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placesDao(): PlacesDao
}