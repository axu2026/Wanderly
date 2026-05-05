package com.example.wanderly.viewmodel

import android.app.Application
import android.location.Address
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.api.WikiRetrofitInstance
import com.example.wanderly.api.WikiSummary
import com.example.wanderly.repository.InformationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// information view model
class InformationViewModel(application: Application) : AndroidViewModel(application) {
    // information repository
    private val repository = InformationRepository(WikiRetrofitInstance.restApi, WikiRetrofitInstance.geoApi)

    // stateflow values
    // location information
    private val _locationInfo = MutableStateFlow<WikiSummary?>(null)
    val locationInfo: StateFlow<WikiSummary?> = _locationInfo
    private val _locationInfoIsLoading = MutableStateFlow(false)
    val locationInfoIsLoading: StateFlow<Boolean> = _locationInfoIsLoading

    // important places information
    private val _importantPlacesInfo = MutableStateFlow<List<WikiSummary?>>(emptyList())
    val importantPlacesInfo: StateFlow<List<WikiSummary?>> = _importantPlacesInfo
    private val _importantPlacesInfoIsLoading = MutableStateFlow(false)
    val importantPlacesInfoIsLoading: StateFlow<Boolean> = _importantPlacesInfoIsLoading

    // selected place information
    private val _selectedPlaceInfo = MutableStateFlow<WikiSummary?>(null)
    val selectedPlaceInfo: StateFlow<WikiSummary?> = _selectedPlaceInfo
    private val _selectedPlaceInfoIsLoading = MutableStateFlow(false)
    val selectedPlaceInfoIsLoading: StateFlow<Boolean> = _selectedPlaceInfoIsLoading

    // fetch information for user's location
    fun fetchLocationInformation(address: Address) {
        viewModelScope.launch {
            _locationInfo.value = null
            _locationInfoIsLoading.value = true
            
            // Create a list of potential Wikipedia page titles from the address
            val candidates = mutableListOf<String>()
            
            if (!address.locality.isNullOrBlank() && !address.adminArea.isNullOrBlank()) {
                candidates.add("${address.locality}, ${address.adminArea}")
            }
            
            if (!address.locality.isNullOrBlank()) {
                candidates.add(address.locality!!)
            }
            
            if (!address.subAdminArea.isNullOrBlank()) {
                candidates.add(address.subAdminArea!!)
            }
            
            if (!address.adminArea.isNullOrBlank()) {
                candidates.add(address.adminArea!!)
            }
            
            if (!address.countryName.isNullOrBlank()) {
                candidates.add(address.countryName!!)
            }

            var success = false
            for (title in candidates) {
                try {
                    val result = repository.getSummary(title)
                    _locationInfo.value = result
                    success = true
                    break
                } catch (e: Exception) {
                    Log.d("InformationViewModel", "Wikipedia page not found for title: $title")
                }
            }
            
            if (!success) {
                _locationInfo.value = null
            }
            
            _locationInfoIsLoading.value = false
        }
    }

    // fetch information for all important places
    fun fetchImportantPlacesInformation(places: List<PlaceResult>) {
        viewModelScope.launch {
            _importantPlacesInfo.value = emptyList()
            if (places.isEmpty()) {
                return@launch
            }

            _importantPlacesInfoIsLoading.value = true

            try {
                // Fetch summaries in parallel for better performance
                coroutineScope {
                    val summaries = places.map { place ->
                        async {
                            try {
                                repository.getNearbySummary(
                                    place.geometry.location.lat,
                                    place.geometry.location.lng
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll()
                    _importantPlacesInfo.value = summaries
                }
            } catch (e: Exception) {
                Log.e("InformationViewModel", "Error fetching places info", e)
            } finally {
                _importantPlacesInfoIsLoading.value = false
            }
        }
    }

    // fetch information for a specific place
    fun fetchPlaceInformation(place: PlaceResult) {
        viewModelScope.launch {
            _selectedPlaceInfo.value = null
            _selectedPlaceInfoIsLoading.value = true
            try {
                val result = repository.getNearbySummary(
                    place.geometry.location.lat,
                    place.geometry.location.lng
                )
                _selectedPlaceInfo.value = result
            } catch (e: Exception) {
                Log.e("InformationViewModel", "Error fetching place info", e)
            } finally {
                _selectedPlaceInfoIsLoading.value = false
            }
        }
    }
}
