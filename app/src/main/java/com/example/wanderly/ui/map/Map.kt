package com.example.wanderly.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun Map(
    userCoordinates: LatLng,
    targetLocation: LatLng? = null,
    focusedPlace: Place? = null,
    focusedDay: ItineraryDay? = null,
    transportMode: String = "Walking",
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(targetLocation ?: userCoordinates)
            .zoom(15f)
            .build()
    }

    // Animate to target location (Home recommendation tap)
    LaunchedEffect(targetLocation) {
        targetLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 16f),
                durationMs = 1000,
            )
        }
    }

    // Fit camera to a whole day's stops (Itinerary day-header tap)
    LaunchedEffect(focusedDay?.dayNumber, focusedDay?.items?.size) {
        val stops = focusedDay?.items.orEmpty()
        if (stops.isNotEmpty()) {
            if (stops.size == 1) {
                val s = stops.first()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(s.place.latitude, s.place.longitude), 15f)
                )
            } else {
                val builder = LatLngBounds.builder()
                stops.forEach { builder.include(LatLng(it.place.latitude, it.place.longitude)) }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), 160)
                )
            }
        }
    }

    // Zoom to a single focused place (Itinerary card tap)
    LaunchedEffect(focusedPlace?.name, focusedPlace?.latitude, focusedPlace?.longitude) {
        if (focusedDay == null) {
            focusedPlace?.let {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            // User location marker — always shown (stylized)
            val userMarkerState = rememberMarkerState(key = "user", position = userCoordinates)
            MarkerComposable(state = userMarkerState, title = "You") {
                UserLocationIcon()
            }

            // Numbered markers for an entire day's stops
            if (focusedDay != null) {
                focusedDay.items.forEachIndexed { index, item ->
                    Marker(
                        state = MarkerState(position = LatLng(item.place.latitude, item.place.longitude)),
                        title = "${index + 1}. ${item.place.name}",
                        snippet = item.place.address,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    )
                }
            } else {
                // Single focused place from itinerary card
                focusedPlace?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                        title = it.name,
                        snippet = it.address,
                    )
                }
                // Target location from Home recommendations
                targetLocation?.let {
                    val targetMarkerState = rememberMarkerState(key = "target", position = it)
                    MarkerComposable(state = targetMarkerState) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }
        }

        if (focusedDay != null && focusedDay.items.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = {
                    val url = buildDirectionsUrl(focusedDay, transportMode)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    runCatching { context.startActivity(intent) }
                        .onFailure { context.startActivity(fallback) }
                },
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                text = { Text("Open route in Google Maps") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun UserLocationIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun buildDirectionsUrl(day: ItineraryDay, transportMode: String): String {
    val stops = day.items.map { it.place }
    val travelMode = when (transportMode) {
        "Car" -> "driving"
        "Transit" -> "transit"
        else -> "walking"
    }
    // Google Maps requires raw "lat,lng" with literal commas and "|" between waypoints —
    // do NOT percent-encode these separators or the app falls back to its default view.
    val origin = "${stops.first().latitude},${stops.first().longitude}"
    val destination = "${stops.last().latitude},${stops.last().longitude}"
    val waypoints = stops.drop(1).dropLast(1)
        .joinToString("|") { "${it.latitude},${it.longitude}" }

    return buildString {
        append("https://www.google.com/maps/dir/?api=1")
        append("&origin=").append(origin)
        append("&destination=").append(destination)
        if (waypoints.isNotEmpty()) {
            append("&waypoints=").append(waypoints)
        }
        append("&travelmode=").append(travelMode)
    }
}
