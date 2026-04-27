package com.example.wanderly.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// view model for location
class LocationViewModel(application: Application) : AndroidViewModel(application) {
    data class LocationState(
        val latitude: Double? = null,
        val longitude: Double? = null
    )

    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _state

    private var lastEmittedLocation: Location? = null

    private val minDistanceMeters = 5f 

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    
    private var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .setMinUpdateIntervalMillis(2000L)
        .setWaitForAccurateLocation(false)
        .build()
        
    private var locationCallback: LocationCallback? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { updateState(it) }
            }
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error: ${e.message}")
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { updateState(it) }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error: ${e.message}")
        }
    }

    private fun updateState(loc: Location) {
        val previous = lastEmittedLocation
        
        // Noise filter logic
        if (previous != null && previous.distanceTo(loc) < minDistanceMeters) return

        Log.d("LocationViewModel", "New Location: ${loc.latitude}, ${loc.longitude}")
        lastEmittedLocation = loc
        _state.value = LocationState(loc.latitude, loc.longitude)
    }

    fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
