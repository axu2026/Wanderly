package com.example.wanderly.api

import retrofit2.http.GET
import retrofit2.http.Query

// api result from calling Weather API
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

// helper classes for api result
data class Main(
    val temp: Double
)

data class Weather(
    val description: String
)

// api interface for calling Weather API
interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

// retrofit instance for Weather API
object WeatherRetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val api: WeatherApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(WeatherApi::class.java)
    }
}