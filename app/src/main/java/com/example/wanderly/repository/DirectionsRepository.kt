package com.example.wanderly.repository

import com.example.wanderly.api.DirectionsApi
import com.example.wanderly.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

class DirectionsRepository(
    private val api: DirectionsApi
) {
    suspend fun getDirections(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        mode: String = "walking",
        waypoints: List<LatLng> = emptyList()
    ): List<LatLng> {
        val waypointsString = if (waypoints.isNotEmpty()) {
            "optimize:false|" + waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
        } else {
            null
        }

        val response = api.getDirections(
            origin = "$lat1,$lon1",
            destination = "$lat2,$lon2",
            mode = mode,
            waypoints = waypointsString,
            apiKey = BuildConfig.MAPS_API_KEY
        )

        val encodedPolyline = response.routes
            .firstOrNull()
            ?.overview_polyline
            ?.points
            ?: return emptyList()

        return PolyUtil.decode(encodedPolyline)
    }
}