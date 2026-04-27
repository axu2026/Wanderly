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

    // last location and min distance before recalling api
    private var lastEmittedLocation: Location? = null
    private val minDistanceMeters = 50f // Reduced for better emulator testing

    // location services
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .setMinUpdateIntervalMillis(2000L)
        .build()
    private var locationCallback: LocationCallback? = null

    // start location updates
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        // 1. Get last known location immediately to prevent hanging
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { updateState(it) }
            }
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error getting last location: ${e.message}")
        }

        // 2. Setup periodic updates
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
            Log.e("LocationViewModel", "Error requesting updates: ${e.message}")
        }
    }

    private fun updateState(loc: Location) {
        val previous = lastEmittedLocation
        if (previous != null && previous.distanceTo(loc) < minDistanceMeters) return

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
