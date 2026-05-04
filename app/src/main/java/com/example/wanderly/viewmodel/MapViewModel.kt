package com.example.wanderly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.DirectionsRetrofitClient
import com.example.wanderly.repository.DirectionsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = DirectionsRepository(DirectionsRetrofitClient.api)

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints

    fun fetchRoute(origin: LatLng, destination: LatLng, mode: String = "walking") {
        viewModelScope.launch {
            try {
                val points = repository.getDirections(
                    origin.latitude, origin.longitude,
                    destination.latitude, destination.longitude,
                    mode
                )
                _routePoints.value = points
            } catch (e: Exception) {
                _routePoints.value = emptyList()
            }
        }
    }

    fun clearRoute() {
        _routePoints.value = emptyList()
    }
}
