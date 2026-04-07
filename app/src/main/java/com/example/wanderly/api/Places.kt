package com.example.wanderly.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.Query

data class Location(
    val address: String? = null,
    val locality: String? = null,
    val region: String? = null
)

data class Category(
    val id: Int,
    val name: String
)

data class Place(
    @SerializedName("fsq_id")
    val id: String,
    val name: String,
    val distance: Int?,
    val categories: List<Category> = emptyList(),
    val location: Location? = null
)

data class PlacesSearchResponse(
    val results: List<Place>
)

interface FoursquareApi {
    @GET("places/search")
    suspend fun searchNearby(
        @Query("ll") ll: String,
        @Query("radius") radius: Int = 1000,
        @Query("query") query: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "fsq_id,name,distance,categories,location"
    ): PlacesSearchResponse
}

object FoursquareClient {

    private const val BASE_URL = "https://api.foursquare.com/v3/places"

    val api: FoursquareApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader(
                                "Authorization",
                                ""
                            )
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()
            .create(FoursquareApi::class.java)
    }
}

class PlacesRepository {
    suspend fun getNearbyPlaces(lat: Double, lon: Double, query: String? = null): List<Place> {
        val response = FoursquareClient.api.searchNearby(
            ll = "$lat,$lon",
            query = query,
            radius = 3000,
            limit = 25
        )
        return response.results
    }
}

fun formatDistance(meters: Int?): String {
    return when {
        meters == null -> ""
        meters < 1000 -> "$meters m"
        else -> String.format("%.1f km", meters / 1000.0)
    }
}