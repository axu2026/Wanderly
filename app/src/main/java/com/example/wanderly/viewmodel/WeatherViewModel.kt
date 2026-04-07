package com.example.wanderly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel: ViewModel() {
    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchWeather(coordinates: LatLng) {
        viewModelScope.launch {
            _isLoading.value = true
            val lat = coordinates.latitude
            val lon = coordinates.longitude

            try {
                val result = RetrofitInstance.api.getWeather(
                    lat,
                    lon,
                    "" // change eventually
                )
                _weather.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _isLoading.value = false
        }
    }
}