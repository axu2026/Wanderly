package com.example.wanderly.api

import com.google.android.gms.maps.model.LatLng
import retrofit2.http.GET
import retrofit2.http.Query

// api result from calling Places API
data class PlacesResponse(
    val results: List<PlaceResult>,
    val status: String,
    val next_page_token: String?
)

// result of a single place
data class PlaceResult(
    val name: String,
    val place_id: String,
    val geometry: Geometry,
    val rating: Double?,
    val vicinity: String?,
    val types: List<String>,
    val photos: List<Photo>?
)

// helper classes for api result
data class Geometry(
    val location: LatLng
)

data class Photo(
    val photo_reference: String
)

// api interface for calling Places API
interface PlacesApi {
    @GET("place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 1500,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}

// retrofit instance for Places API
object PlacesRetrofitInstance {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val api: PlacesApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(PlacesApi::class.java)
    }
}