package com.example.wanderly.local.places

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlacesDao {
    @Query("SELECT * FROM places WHERE `key` = :key")
    suspend fun getCache(key: String): PlacesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(places: PlacesEntity)
}