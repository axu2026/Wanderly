package com.example.wanderly.api

import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// define data type for wikipedia response
data class WikiSummary(
    val title: String,
    val extract: String,
    val type: String?,
    val thumbnail: Thumbnail?
)

data class Thumbnail(
    val source: String
)

data class GeoSearchResponse(
    val query: GeoQuery
)

data class GeoQuery(
    val geosearch: List<GeoPage>
)

data class GeoPage(
    val pageid: Int,
    val title: String
)

data class SummaryResponse(
    val title: String,
    val extract: String?,
    val type: String? // "standard" or "disambiguation"
)

// interface defining endpoints to contact wikipedia
interface WikiApi {
    @GET("api/rest_v1/page/summary/{title}")
    suspend fun getSummary(
        @Path("title") title: String
    ): WikiSummary
}

// separate interface to get geolocation info
interface GeoWikiApi {
    @GET("w/api.php?action=query&list=geosearch&format=json")
    suspend fun geoSearch(
        @Query("gscoord") coords: String,   // "lat|lon"
        @Query("gsradius") radius: Int = 100,
        @Query("gslimit") limit: Int = 5
    ): GeoSearchResponse
}

// retrofit instance to make requests
object WikiRetrofitInstance {
    private const val BASE_URL = "https://en.wikipedia.org/"

    // wikipedia wants to know where requests are coming from, provide a client
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Wanderly/1.0 (axu26@bu.edu) Android App")
                .build()
            chain.proceed(request)
        }
        .build()

    val restApi: WikiApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(WikiApi::class.java)
    }

    val geoApi: GeoWikiApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(GeoWikiApi::class.java)
    }
}
