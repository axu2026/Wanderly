package com.example.wanderly.api

import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.Path

data class WikiSummary(
    val title: String,
    val extract: String,
    val thumbnail: Thumbnail?
)

data class Thumbnail(
    val source: String
)

interface WikiApi {
    @GET("page/summary/{title}")
    suspend fun getSummary(
        @Path("title") title: String
    ): WikiSummary
}

object WikiRetrofitInstance {
    private const val BASE_URL = "https://en.wikipedia.org/api/rest_v1/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Wanderly/1.0 (contact@example.com) Android App")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: WikiApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(WikiApi::class.java)
    }
}
