package com.example.wanderly.local.places

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlacesEntity(
    @PrimaryKey
    val key: String,
    val json: String,
    val timestamp: Long
)