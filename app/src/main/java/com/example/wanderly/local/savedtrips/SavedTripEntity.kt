package com.example.wanderly.local.savedtrips

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_trips")
data class SavedTripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val city: String,
    val days: Int,
    val stopCount: Int,
    val totalCost: Double,
    val transportMode: String,
    val savedAt: Long,
    val payloadJson: String,
)
