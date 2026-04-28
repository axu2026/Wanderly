package com.example.wanderly.repository

import com.example.wanderly.BuildConfig
import com.example.wanderly.api.PlacesApi

class HomeRepository(private val api: PlacesApi) {
    suspend fun getCityImageUrl(cityName: String): String? {
        return try {
            val response = api.findPlace(
                query = cityName,
                apiKey = BuildConfig.MAPS_API_KEY
            )
            val photoReference = response.results.firstOrNull()?.photos?.firstOrNull()?.photo_reference
            if (photoReference != null) {
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=1600&photo_reference=$photoReference&key=${BuildConfig.MAPS_API_KEY}"
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}