package com.example.wanderly.repository

import com.example.wanderly.BuildConfig
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.api.PlacesApi
import com.example.wanderly.local.places.PlacesDao
import com.example.wanderly.local.places.PlacesEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// repository for Places API
class PlacesRepository(
    private val api: PlacesApi,
    private val dao: PlacesDao
) {
    // cache for places api
    private val gson = Gson()
    private val listType = object : TypeToken<List<PlaceResult>>() {}.type

    // get nearby places from cache or api
    suspend fun getNearbyPlaces(lat: Double, lon: Double, type: String): List<PlaceResult> {
        val key = generateKey(lat, lon, type)
        val cached = dao.getCache(key)

        // if cached is less than 24 hours old, return cached
        if (cached != null) {
            if (System.currentTimeMillis() - cached.timestamp < 24 * 60 * 60 * 1000) {
                return decode(cached.json)
            }
        }

        // if not cached, fetch from api
        val response = api.getNearbyPlaces(
            location = "$lat,$lon",
            type = type,
            apiKey = BuildConfig.MAPS_API_KEY
        )

        // if error, throw exception
        if (response.status != "OK" && response.status != "ZERO_RESULTS") {
            throw Exception("Places API error: ${response.status}")
        }

        val results = response.results

        // cache the results
        dao.insertCache(
            PlacesEntity(
                key = key,
                json = encode(results),
                timestamp = System.currentTimeMillis()
            )
        )

        return results
    }

    // generate a unique key for caching
    private fun generateKey(lat: Double, lon: Double, type: String): String {
        val latKey = (lat * 100).toInt()
        val lonKey = (lon * 100).toInt()
        return "$latKey:$lonKey:$type"
    }

    // encode and decode places for caching
    private fun encode(places: List<PlaceResult>): String {
        return gson.toJson(places)
    }

    private fun decode(json: String): List<PlaceResult> {
        return gson.fromJson(json, listType)
    }
}