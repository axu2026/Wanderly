package com.example.wanderly.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
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

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    data class State(
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
        .setMinUpdateIntervalMillis(5000L)
        .build()
    private var locationCallback: LocationCallback? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return

                _state.value = State(
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

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }

        locationCallback = null
    }

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