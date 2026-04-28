package com.example.wanderly.data.model

data class ItineraryDay(
    val dayNumber: Int,
    val items: List<ItineraryItem>,
    val totalCost: Double
)
