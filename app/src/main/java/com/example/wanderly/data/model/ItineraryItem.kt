package com.example.wanderly.data.model

import java.time.LocalTime

data class ItineraryItem(
    val id: String,
    val place: Place,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val cost: Double,
    val isCompleted: Boolean = false
)
