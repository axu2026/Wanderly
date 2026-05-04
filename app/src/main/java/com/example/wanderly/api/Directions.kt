package com.example.wanderly.api

import retrofit2.http.GET
import retrofit2.http.Query

// api return types
data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val overview_polyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String
)

// api interface for directions
interface DirectionsApi {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String
    ): DirectionsResponse
}

// retrofit client to deal with the call
object DirectionsRetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/"

    val api: DirectionsApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(DirectionsApi::class.java)
    }
}