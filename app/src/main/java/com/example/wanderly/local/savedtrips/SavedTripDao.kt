package com.example.wanderly.local.savedtrips

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTripDao {
    @Query("SELECT * FROM saved_trips ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedTripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: SavedTripEntity): Long

    @Query("SELECT * FROM saved_trips WHERE id = :id")
    suspend fun get(id: Long): SavedTripEntity?

    @Query("DELETE FROM saved_trips WHERE id = :id")
    suspend fun delete(id: Long)
}
