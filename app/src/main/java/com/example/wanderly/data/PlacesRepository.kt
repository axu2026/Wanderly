package com.example.wanderly.data

import android.content.Context
import android.content.pm.PackageManager
import com.example.wanderly.api.NearbySearchRequest
import com.example.wanderly.api.Center
import com.example.wanderly.api.Circle
import com.example.wanderly.api.ItineraryPlacesRetrofit
import com.example.wanderly.api.LocationRestriction
import com.example.wanderly.api.PlaceDto
import com.example.wanderly.data.model.Place
import com.example.wanderly.ui.map.geocodeAddress

object PlacesRepository {

    private const val SEARCH_RADIUS_METERS = 10000.0
    private const val MAX_PER_QUERY = 15

    suspend fun fetchPlacesForCity(
        context: Context,
        city: String,
        interests: Set<String>,
    ): List<Place>? {
        val apiKey = readApiKey(context).takeIf { it.isNotBlank() } ?: return null
        val address = geocodeAddress(context, city) ?: return null
        val center = Center(address.latitude, address.longitude)

        val results = mutableListOf<Place>()
        val claimed = mutableSetOf<String>()

        fun addUnique(places: List<Place>) {
            places.forEach { p ->
                if (claimed.add(p.name.lowercase().trim())) results += p
            }
        }

        // Always fetch meals — different type queries reduce overlap, dedupe handles the rest
        addUnique(
            fetchSlot(apiKey, center, listOf("breakfast_restaurant", "cafe", "bakery"))
                .map { it.toPlace(category = "Food", mealType = "Breakfast", durationMinutes = 45) }
        )
        addUnique(
            fetchSlot(apiKey, center, listOf("meal_takeaway", "sandwich_shop", "fast_food_restaurant"))
                .map { it.toPlace(category = "Food", mealType = "Lunch", durationMinutes = 60) }
                .ifEmpty {
                    fetchSlot(apiKey, center, listOf("restaurant"))
                        .map { it.toPlace(category = "Food", mealType = "Lunch", durationMinutes = 60) }
                }
        )
        addUnique(
            fetchSlot(apiKey, center, listOf("fine_dining_restaurant", "steak_house", "italian_restaurant"))
                .map { it.toPlace(category = "Food", mealType = "Dinner", durationMinutes = 75) }
                .ifEmpty {
                    fetchSlot(apiKey, center, listOf("restaurant"))
                        .map { it.toPlace(category = "Food", mealType = "Dinner", durationMinutes = 75) }
                }
        )

        // Activities — fetch by interest, or a balanced default if none picked
        val activityQueries = if (interests.isEmpty()) {
            listOf(
                "Culture" to listOf("museum", "art_gallery"),
                "Nature" to listOf("park"),
                "Shopping" to listOf("shopping_mall"),
                "Nightlife" to listOf("bar", "night_club"),
            )
        } else {
            interests.mapNotNull { interest ->
                when (interest) {
                    "Culture" -> "Culture" to listOf("museum", "art_gallery")
                    "History" -> "History" to listOf("historical_landmark", "monument", "history_museum")
                    "Nature" -> "Nature" to listOf("park", "national_park", "garden")
                    "Adventure" -> "Adventure" to listOf("amusement_park", "hiking_area", "tourist_attraction")
                    "Shopping" -> "Shopping" to listOf("shopping_mall")
                    "Nightlife" -> "Nightlife" to listOf("bar", "night_club")
                    "Wellness" -> "Wellness" to listOf("spa", "yoga_studio", "wellness_center")
                    "Family" -> "Family" to listOf("zoo", "aquarium", "amusement_park")
                    "Coffee" -> "Coffee" to listOf("cafe", "coffee_shop", "bakery")
                    "Food" -> "Food" to listOf("tourist_attraction") // Food covered by meals
                    else -> null
                }
            }
        }

        for ((category, types) in activityQueries) {
            val duration = when (category) {
                "Culture" -> 150
                "History" -> 120
                "Nature" -> 90
                "Adventure" -> 180
                "Shopping" -> 90
                "Nightlife" -> 90
                "Wellness" -> 90
                "Family" -> 180
                "Coffee" -> 30
                else -> 90
            }
            addUnique(
                fetchSlot(apiKey, center, types)
                    .map { it.toPlace(category = category, mealType = null, durationMinutes = duration) }
            )
        }

        return results.takeIf { it.any { p -> p.mealType != null } && it.any { p -> p.mealType == null } }
    }

    private suspend fun fetchSlot(
        apiKey: String,
        center: Center,
        types: List<String>,
    ): List<PlaceDto> = try {
        val response = ItineraryPlacesRetrofit.api.searchNearby(
            headers = ItineraryPlacesRetrofit.headers(apiKey),
            body = NearbySearchRequest(
                includedTypes = types,
                maxResultCount = MAX_PER_QUERY,
                locationRestriction = LocationRestriction(
                    Circle(center = center, radius = SEARCH_RADIUS_METERS)
                ),
            )
        )
        response.places.orEmpty()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    private fun PlaceDto.toPlace(
        category: String,
        mealType: String?,
        durationMinutes: Int,
    ): Place = Place(
        name = displayName?.text ?: "Unnamed",
        address = formattedAddress ?: "Unknown address",
        latitude = location?.latitude ?: 0.0,
        longitude = location?.longitude ?: 0.0,
        estimatedCost = priceLevelToDollars(priceLevel, category),
        averageDurationMinutes = durationMinutes,
        rating = rating,
        category = category,
        mealType = mealType,
    )

    private fun priceLevelToDollars(priceLevel: String?, category: String): Double = when (priceLevel) {
        "PRICE_LEVEL_FREE" -> 0.0
        "PRICE_LEVEL_INEXPENSIVE" -> 15.0
        "PRICE_LEVEL_MODERATE" -> 35.0
        "PRICE_LEVEL_EXPENSIVE" -> 65.0
        "PRICE_LEVEL_VERY_EXPENSIVE" -> 100.0
        else -> when (category) {
            "Nature" -> 0.0
            "Shopping" -> 0.0
            "Culture" -> 25.0
            "History" -> 20.0
            "Adventure" -> 50.0
            "Nightlife" -> 35.0
            "Wellness" -> 60.0
            "Family" -> 30.0
            "Coffee" -> 8.0
            "Food" -> 25.0
            else -> 0.0
        }
    }

    private fun readApiKey(context: Context): String = try {
        val ai = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        ai.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
    } catch (e: Exception) {
        ""
    }
}
