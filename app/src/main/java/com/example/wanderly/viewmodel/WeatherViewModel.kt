package com.example.wanderly.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.*
import com.example.wanderly.repository.WeatherRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// view model for weather
class WeatherViewModel : ViewModel() {
    // repository for weather
    private val repository = WeatherRepository(WeatherRetrofitInstance.api)

    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var lastFetchTime = 0L
    private var lastFetchLocation: LatLng? = null

    fun fetchWeather(coordinates: LatLng) {
        val currentTime = System.currentTimeMillis()
        val distance = lastFetchLocation?.let { last ->
            val results = FloatArray(1)
            Location.distanceBetween(last.latitude, last.longitude, coordinates.latitude, coordinates.longitude, results)
            results[0]
        } ?: Float.MAX_VALUE

        // Throttle: Only update if 10 minutes have passed OR user moved more than 500m
        if (currentTime - lastFetchTime < 10 * 60 * 1000 && distance < 500f && _weather.value != null) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getWeather(coordinates.latitude, coordinates.longitude)
                _weather.value = result
                lastFetchTime = currentTime
                lastFetchLocation = coordinates
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
