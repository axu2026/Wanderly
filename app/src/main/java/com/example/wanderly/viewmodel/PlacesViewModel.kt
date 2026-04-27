package com.example.wanderly.viewmodel

import android.app.Application
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

// view model for places
class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    // repository for places
    private val database = DatabaseProvider.getDatabase(application)
    private val repository = PlacesRepository(
        PlacesRetrofitInstance.api,
        database.placesDao()
    )

    private val _places = MutableStateFlow<List<PlaceResult>>(emptyList())
    val places: StateFlow<List<PlaceResult>> = _places
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("restaurant")
    val searchQuery: StateFlow<String> = _searchQuery

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchPlaces(coordinates: LatLng, query: String = _searchQuery.value) {
        viewModelScope.launch {
            _isLoading.value = true
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
                _isLoading.value = false
            }
        }
    }
}