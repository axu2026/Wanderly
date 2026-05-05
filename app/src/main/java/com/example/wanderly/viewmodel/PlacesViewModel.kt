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

// view model storing places state
class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    // database and repository for place
    private val database = DatabaseProvider.getDatabase(application)
    private val repository = PlacesRepository(PlacesRetrofitInstance.api, database.placesDao())

    // stateflow values
    // search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // search places
    private val _searchedPlaces = MutableStateFlow<List<PlaceResult>>(emptyList())
    val searchedPlaces: StateFlow<List<PlaceResult>> = _searchedPlaces
    private val _searchIsLoading = MutableStateFlow(false)
    val searchIsLoading: StateFlow<Boolean> = _searchIsLoading

    // important places
    private val _importantPlaces = MutableStateFlow<List<PlaceResult>>(emptyList())
    val importantPlaces: StateFlow<List<PlaceResult>> = _importantPlaces
    private val _importantIsLoading = MutableStateFlow(false)
    val importantIsLoading: StateFlow<Boolean> = _importantIsLoading

    // update search query state
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // clears search query and results
    fun clearSearchResults() {
        _searchQuery.value = ""
        _searchedPlaces.value = emptyList()
    }

    // fetches searched places from PlacesRepository
    fun fetchSearchedPlaces(coordinates: LatLng, query: String = _searchQuery.value) {
        viewModelScope.launch {
            _searchIsLoading.value = true
            try {
                val result = repository.getNearbyPlaces(
                    coordinates.latitude,
                    coordinates.longitude,
                    query
                )
                _searchedPlaces.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _searchIsLoading.value = false
            }
        }
    }

    // fetches important places from PlacesRepository
    fun fetchImportantPlaces(coordinates: LatLng) {
        viewModelScope.launch {
            _importantIsLoading.value = true
            try {
                val result = repository.getImportantSpots(
                    coordinates.latitude,
                    coordinates.longitude
                )
                _importantPlaces.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _importantIsLoading.value = false
            }
        }
    }
}
