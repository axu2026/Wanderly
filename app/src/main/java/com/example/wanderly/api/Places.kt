package com.example.wanderly.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

data class NearbySearchRequest(
    val includedTypes: List<String>,
    val maxResultCount: Int = 10,
    val locationRestriction: LocationRestriction,
    val rankPreference: String = "POPULARITY",
)

data class LocationRestriction(val circle: Circle)
data class Circle(val center: Center, val radius: Double)
data class Center(val latitude: Double, val longitude: Double)

data class NearbySearchResponse(val places: List<PlaceDto>?)

data class PlaceDto(
    val id: String?,
    val displayName: DisplayName?,
    val formattedAddress: String?,
    val location: Center?,
    val rating: Double?,
    val priceLevel: String?,
    val types: List<String>?,
    val userRatingCount: Int?,
)

data class DisplayName(val text: String?, val languageCode: String?)

interface PlacesApi {
    @POST("v1/places:searchNearby")
    suspend fun searchNearby(
        @HeaderMap headers: Map<String, String>,
        @Body body: NearbySearchRequest,
    ): NearbySearchResponse
}

object PlacesRetrofit {
    private const val BASE_URL = "https://places.googleapis.com/"

    val api: PlacesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApi::class.java)
    }

    fun headers(apiKey: String): Map<String, String> = mapOf(
        "X-Goog-Api-Key" to apiKey,
        "X-Goog-FieldMask" to listOf(
            "places.id",
            "places.displayName",
            "places.formattedAddress",
            "places.location",
            "places.rating",
            "places.priceLevel",
            "places.types",
            "places.userRatingCount",
        ).joinToString(","),
        "Content-Type" to "application/json",
    )
}
