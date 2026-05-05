package com.example.wanderly.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wanderly.local.places.PlacesDao
import com.example.wanderly.local.places.PlacesEntity
import com.example.wanderly.local.savedtrips.SavedTripDao
import com.example.wanderly.local.savedtrips.SavedTripEntity
import com.example.wanderly.local.users.UserDao
import com.example.wanderly.local.users.UserEntity

@Database(
    entities = [PlacesEntity::class, SavedTripEntity::class, UserEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placesDao(): PlacesDao
    abstract fun savedTripDao(): SavedTripDao
    abstract fun userDao(): UserDao
}
