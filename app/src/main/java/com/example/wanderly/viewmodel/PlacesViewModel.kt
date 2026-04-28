package com.example.wanderly.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.api.PlacesRetrofitInstance
import com.example.wanderly.local.DatabaseProvider
import com.example.wanderly.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val repository = PlacesRepository(
        PlacesRetrofitInstance.api,
        database.placesDao()
    )

    private val _places = MutableStateFlow<List<PlaceResult>>(emptyList())
    val places: StateFlow<List<PlaceResult>> = _places
    
    private val _placesIsLoading = MutableStateFlow(false)
    val placesIsLoading: StateFlow<Boolean> = _placesIsLoading

    private val _importantSpots = MutableStateFlow<List<PlaceResult>>(emptyList())
    val importantSpots: StateFlow<List<PlaceResult>> = _importantSpots

    private val _importantIsLoading = MutableStateFlow(false)
    val importantIsLoading: StateFlow<Boolean> = _importantIsLoading

    private val _closestImportantSpot = MutableStateFlow<PlaceResult?>(null)
    val closestImportantSpot: StateFlow<PlaceResult?> = _closestImportantSpot

    private val _searchQuery = MutableStateFlow("restaurant")
    val searchQuery: StateFlow<String> = _searchQuery

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchPlaces(coordinates: LatLng, query: String = _searchQuery.value) {
        viewModelScope.launch {
            _placesIsLoading.value = true
            try {
                val result = repository.getNearbyPlaces(
                    coordinates.latitude,
                    coordinates.longitude,
                    query
                )
                _places.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _placesIsLoading.value = false
            }
        }
    }

    fun fetchImportantSpots(coordinates: LatLng) {
        viewModelScope.launch {
            _importantIsLoading.value = true
            try {
                val result = repository.getImportantSpots(
                    coordinates.latitude,
                    coordinates.longitude
                )
                _importantSpots.value = result
                
                // Find the closest spot to display detailed info
                if (result.isNotEmpty()) {
                    _closestImportantSpot.value = result.minByOrNull { spot ->
                        val distanceResults = FloatArray(1)
                        Location.distanceBetween(
                            coordinates.latitude, coordinates.longitude,
                            spot.geometry.location.lat, spot.geometry.location.lng,
                            distanceResults
                        )
                        distanceResults[0]
                    }
                } else {
                    _closestImportantSpot.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _importantIsLoading.value = false
            }
        }
    }
}
