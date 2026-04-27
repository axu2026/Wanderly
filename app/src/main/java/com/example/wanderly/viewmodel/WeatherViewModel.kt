package com.example.wanderly.viewmodel

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

    // fetch weather from repository
    fun fetchWeather(coordinates: LatLng) {
        viewModelScope.launch {
            _isLoading.value = true
            val lat = coordinates.latitude
            val lon = coordinates.longitude

            try {
                val result = repository.getWeather(lat, lon)
                _weather.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}