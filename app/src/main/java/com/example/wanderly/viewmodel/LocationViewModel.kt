package com.example.wanderly.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
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
    private val minDistanceMeters = 150f

    // location services
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
        .setMinUpdateIntervalMillis(5000L)
        .build()
    private var locationCallback: LocationCallback? = null

    // start location updates
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return
                val previous = lastEmittedLocation

                // if location is too close to previous location, ignore
                if (previous != null) {
                    val distance = previous.distanceTo(loc)

                    if (distance < minDistanceMeters) {
                        return
                    }
                }

                // update last location
                lastEmittedLocation = loc

                _state.value = LocationState(
                    loc.latitude,
                    loc.longitude,
                )
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    // stop using location services
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }

        locationCallback = null
    }

    // check if app has permission to use location services
    fun hasLocationPermission(): Boolean {
        val context = getApplication<Application>()
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}