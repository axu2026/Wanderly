package com.example.wanderly.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wanderly.local.places.PlacesDao
import com.example.wanderly.local.places.PlacesEntity
import com.example.wanderly.local.savedtrips.SavedTripDao
import com.example.wanderly.local.savedtrips.SavedTripEntity

@Database(
    entities = [PlacesEntity::class, SavedTripEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placesDao(): PlacesDao
    abstract fun savedTripDao(): SavedTripDao
}
