package com.example.wanderly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.DirectionsRetrofitClient
import com.example.wanderly.repository.DirectionsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// viewmodel for the map screen, storing routes
class MapViewModel : ViewModel() {
    // repository for fetching route data
    private val repository = DirectionsRepository(DirectionsRetrofitClient.api)

    // store route and send to Polyline via stateflow
    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints

    // get the route from the Directions API
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

    // get the route for the itinerary via Directions API
    fun fetchItineraryRoute(stops: List<LatLng>, mode: String = "walking") {
        if (stops.size < 2) {
            _routePoints.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val origin = stops.first()
                val destination = stops.last()
                val waypoints = if (stops.size > 2) stops.subList(1, stops.size - 1) else emptyList()

                val points = repository.getDirections(
                    origin.latitude, origin.longitude,
                    destination.latitude, destination.longitude,
                    mode,
                    waypoints
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
