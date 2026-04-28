package com.example.wanderly.repository

import com.example.wanderly.BuildConfig
import com.example.wanderly.api.WeatherApi
import com.example.wanderly.api.WeatherResponse

// repository for Weather API
class WeatherRepository(private val api: WeatherApi) {
    // get weather from api
    suspend fun getWeather(lat: Double, lng: Double): WeatherResponse {
        return api.getWeather(lat, lng, BuildConfig.OPENWEATHER_API_KEY)
    }
}