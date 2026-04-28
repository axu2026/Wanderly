package com.example.wanderly.viewmodel

import android.content.Context
import android.location.Address
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.api.PlacesRetrofitInstance
import com.example.wanderly.repository.HomeRepository
import com.example.wanderly.ui.map.getBestLocationName
import com.example.wanderly.ui.map.reverseGeocode
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// view model for home screen
class HomeViewModel: ViewModel() {
    private val repository = HomeRepository(PlacesRetrofitInstance.api)
    
    var address by mutableStateOf<Address?>(null)
    var isLoading by mutableStateOf(false)
    var backgroundImageUrl by mutableStateOf<String?>(null)
    private var lastCoordinates: LatLng? = null

    // fetch address from coordinates
    fun geocodeAddressIfNeeded(context: Context, coordinates: LatLng) {
        // if coordinates are the same, return
        if (address != null && coordinates == lastCoordinates) return

        // update coordinates
        lastCoordinates = coordinates

        // fetch address from coordinates using coroutine
        viewModelScope.launch {
            isLoading = true
            val resolvedAddress = withContext(Dispatchers.IO) {
                reverseGeocode(context, coordinates.latitude, coordinates.longitude)
            }
            address = resolvedAddress
            
            // Once address is resolved, try to fetch a background image
            resolvedAddress?.let {
                val cityName = getBestLocationName(it)
                if (cityName != null) {
                    backgroundImageUrl = repository.getCityImageUrl(cityName)
                }
            }

            isLoading = false
        }
    }
}