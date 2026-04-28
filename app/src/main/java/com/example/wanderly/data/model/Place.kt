package com.example.wanderly.data.model

data class Place(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val estimatedCost: Double,
    val averageDurationMinutes: Int,
    val rating: Double? = null,
    val category: String? = null,
    val mealType: String? = null
)
