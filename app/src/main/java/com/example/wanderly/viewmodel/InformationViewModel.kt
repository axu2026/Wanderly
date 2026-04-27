package com.example.wanderly.viewmodel

import android.location.Address
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.WikiRetrofitInstance
import com.example.wanderly.api.WikiSummary
import com.example.wanderly.repository.InformationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InformationViewModel: ViewModel() {
    private val repository = InformationRepository(WikiRetrofitInstance.api)

    private val _information = MutableStateFlow<WikiSummary?>(null)
    val information: StateFlow<WikiSummary?> = _information
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchInformation(address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Create a list of potential Wikipedia page titles from the address
            val candidates = mutableListOf<String>()
            
            // 1. Specific locality with state/province for disambiguation (e.g., "San Francisco, California")
            if (!address.locality.isNullOrBlank() && !address.adminArea.isNullOrBlank()) {
                candidates.add("${address.locality}, ${address.adminArea}")
            }
            
            // 2. Just the city/locality
            if (!address.locality.isNullOrBlank()) {
                candidates.add(address.locality!!)
            }
            
            // 3. Sub-administrative area (like a county or district)
            if (!address.subAdminArea.isNullOrBlank()) {
                candidates.add(address.subAdminArea!!)
            }
            
            // 4. State/Province
            if (!address.adminArea.isNullOrBlank()) {
                candidates.add(address.adminArea!!)
            }
            
            // 5. Country as a final fallback
            if (!address.countryName.isNullOrBlank()) {
                candidates.add(address.countryName!!)
            }

            var success = false
            for (title in candidates) {
                try {
                    val result = repository.getSummary(title)
                    // If we get here, it means we found a valid summary
                    _information.value = result
                    success = true
                    break
                } catch (e: Exception) {
                    // Log and try the next candidate
                    Log.d("InformationViewModel", "Wikipedia page not found for title: $title")
                }
            }
            
            if (!success) {
                _information.value = null
            }
            
            _isLoading.value = false
        }
    }
}