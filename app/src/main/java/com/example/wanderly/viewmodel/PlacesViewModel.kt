package com.example.wanderly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val repository: PlacesRepository = PlacesRepository()
) : ViewModel() {
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun searchNearby(coordinates: LatLng, query: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val lat = coordinates.latitude
            val lon = coordinates.longitude

            try {
                val list = repository.getNearbyPlaces(lat, lon, query)
                _places.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}