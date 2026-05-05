package com.example.wanderly.local.savedtrips

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTripDao {
    @Query("SELECT * FROM saved_trips WHERE userId = :userId ORDER BY savedAt DESC")
    fun observeForUser(userId: Long): Flow<List<SavedTripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: SavedTripEntity): Long

    @Query("SELECT * FROM saved_trips WHERE id = :id AND userId = :userId")
    suspend fun get(id: Long, userId: Long): SavedTripEntity?

    @Query("DELETE FROM saved_trips WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: Long)
}
