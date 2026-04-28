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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InformationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InformationRepository(WikiRetrofitInstance.restApi, WikiRetrofitInstance.geoApi)

    private val _information = MutableStateFlow<WikiSummary?>(null)
    val information: StateFlow<WikiSummary?> = _information

    private val _informationIsLoading = MutableStateFlow(false)
    val informationIsLoading: StateFlow<Boolean> = _informationIsLoading

    private val _closestInformation = MutableStateFlow<WikiSummary?>(null)
    val closestInformation: StateFlow<WikiSummary?> = _closestInformation

    private val _closestIsLoading = MutableStateFlow(false)
    val closestIsLoading: StateFlow<Boolean> = _closestIsLoading

    fun fetchInformation(address: Address) {
        viewModelScope.launch {
            _informationIsLoading.value = true
            
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
                    _information.value = result
                    success = true
                    break
                } catch (e: Exception) {
                    Log.d("InformationViewModel", "Wikipedia page not found for title: $title")
                }
            }
            
            if (!success) {
                _information.value = null
            }
            
            _informationIsLoading.value = false
        }
    }

    fun fetchClosestInformation(spot: PlaceResult?) {
        viewModelScope.launch {
            if (spot == null) {
                _closestInformation.value = null
                return@launch
            }

            _closestIsLoading.value = true

            try {
                Log.d("InformationViewModel", "Trying Wikipedia title: ${spot.name}")
                val result = repository.getNearbySummary(spot.geometry.location.lat, spot.geometry.location.lng)
                _closestInformation.value = result
            } catch (e: Exception) {
                Log.d("InformationViewModel", "Not found: ${spot.name}")
            } finally {
                _closestIsLoading.value = false
            }
        }
    }
}
