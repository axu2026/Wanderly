package com.example.wanderly.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.Place
import com.example.wanderly.ui.components.PlaceDetailSheet
import com.example.wanderly.ui.home.PlaceCard
import com.example.wanderly.viewmodel.InformationViewModel
import com.example.wanderly.viewmodel.MapViewModel
import com.example.wanderly.viewmodel.PlacesViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.MapUiSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Map(
    userCoordinates: LatLng,
    targetLocation: LatLng? = null,
    focusedPlace: Place? = null,
    focusedDay: ItineraryDay? = null,
    transportMode: String = "Walking",
    viewModel: MapViewModel = viewModel(),
    placesViewModel: PlacesViewModel = viewModel(),
    informationViewModel: InformationViewModel = viewModel(),
    onClearFocus: () -> Unit = {}
) {
    val context = LocalContext.current
    val routePoints by viewModel.routePoints.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(targetLocation ?: userCoordinates)
            .zoom(15f)
            .build()
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    // search state
    val searchQuery by placesViewModel.searchQuery.collectAsState()
    val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()
    val searchIsLoading by placesViewModel.searchIsLoading.collectAsState()

    // Bottom sheet state
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiSettings by remember { mutableStateOf(
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true
        )
    )}

    // Unified routing and camera logic
    LaunchedEffect(targetLocation, focusedPlace, focusedDay, transportMode) {
        val mode = when (transportMode) {
            "Car" -> "driving"
            "Transit" -> "transit"
            else -> "walking"
        }

        when {
            focusedDay != null -> {
                val stops = focusedDay.items.map { LatLng(it.place.latitude, it.place.longitude) }
                if (stops.isNotEmpty()) {
                    viewModel.fetchItineraryRoute(stops, mode)
                    if (stops.size == 1) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(stops.first(), 15f)
                        )
                    } else {
                        val builder = LatLngBounds.builder()
                        stops.forEach { builder.include(it) }
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(builder.build(), 160)
                        )
                    }
                } else {
                    viewModel.clearRoute()
                }
            }
            targetLocation != null -> {
                viewModel.fetchRoute(userCoordinates, targetLocation, mode)
                val builder = LatLngBounds.builder()
                    .include(userCoordinates)
                    .include(targetLocation)
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(builder.build(), 200),
                    durationMs = 1000,
                )
            }
            focusedPlace != null -> {
                val destination = LatLng(focusedPlace.latitude, focusedPlace.longitude)
                viewModel.fetchRoute(userCoordinates, destination, mode)
                val builder = LatLngBounds.builder()
                    .include(userCoordinates)
                    .include(destination)
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(builder.build(), 200),
                    durationMs = 1000,
                )
            }
            else -> {
                viewModel.clearRoute()
            }
        }
    }

    // Refine camera to fit the actual route points once loaded
    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            routePoints.forEach { builder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(builder.build(), 200),
                durationMs = 1000,
            )
        }
    }

    // Fit camera to search results
    LaunchedEffect(searchedPlaces) {
        if (searchedPlaces.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            builder.include(userCoordinates)
            searchedPlaces.forEach { 
                builder.include(LatLng(it.geometry.location.lat, it.geometry.location.lng))
            }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(builder.build(), 200),
                durationMs = 1000
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings
        ) {
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 12f
                )
            }
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
                // Search result markers
                searchedPlaces.forEach { place ->
                    Marker(
                        state = MarkerState(position = LatLng(place.geometry.location.lat, place.geometry.location.lng)),
                        title = place.name,
                        snippet = place.vicinity,
                        onClick = {
                            selectedPlace = place
                            informationViewModel.fetchPlaceInformation(place)
                            showBottomSheet = true
                            false
                        }
                    )
                }

                // Single focused place from itinerary card
                focusedPlace?.let {
                    val placeMarkerState = rememberMarkerState(key = "focused", position = LatLng(it.latitude, it.longitude))
                    MarkerComposable(state = placeMarkerState) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp),
                        )
                    }
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

        // Floating Search Bar
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { placesViewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for places (e.g. cafe, park)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { placesViewModel.clearSearchResults() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else if (targetLocation != null || focusedPlace != null || focusedDay != null) {
                        IconButton(onClick = { onClearFocus() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear route",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onClearFocus()
                        placesViewModel.fetchSearchedPlaces(userCoordinates)
                        keyboardController?.hide()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Horizontal Results Carousel
        if (searchedPlaces.isNotEmpty()) {
            val bottomPadding = if (focusedDay != null && focusedDay.items.isNotEmpty()) 100.dp else 24.dp
            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPadding)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchedPlaces) { place ->
                    val distance = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userCoordinates.latitude, userCoordinates.longitude,
                        place.geometry.location.lat, place.geometry.location.lng,
                        distance
                    )
                    PlaceCard(
                        place = place,
                        distance = distance[0],
                        onClick = {
                            selectedPlace = place
                            informationViewModel.fetchPlaceInformation(place)
                            showBottomSheet = true
                        }
                    )
                }
            }
        }

        if ((focusedDay != null && focusedDay.items.isNotEmpty()) || targetLocation != null || focusedPlace != null) {
            ExtendedFloatingActionButton(
                onClick = {
                    val url = buildDirectionsUrl(userCoordinates, focusedDay, targetLocation, focusedPlace, transportMode)
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

        if (showBottomSheet && selectedPlace != null) {
            val wikiSummary by informationViewModel.selectedPlaceInfo.collectAsState()
            val wikiLoading by informationViewModel.selectedPlaceInfoIsLoading.collectAsState()

            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                PlaceDetailSheet(
                    place = selectedPlace!!,
                    wikiSummary = wikiSummary,
                    isLoading = wikiLoading,
                    onViewOnMap = {
                        showBottomSheet = false
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.geometry.location.lat, it.geometry.location.lng),
                                17f
                            )
                        )
                    }
                )
            }
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

private fun buildDirectionsUrl(
    userCoordinates: LatLng,
    day: ItineraryDay?,
    targetLocation: LatLng?,
    focusedPlace: Place?,
    transportMode: String
): String {
    val travelMode = when (transportMode) {
        "Car" -> "driving"
        "Transit" -> "transit"
        else -> "walking"
    }

    return buildString {
        append("https://www.google.com/maps/dir/?api=1")
        append("&travelmode=").append(travelMode)

        when {
            day != null && day.items.isNotEmpty() -> {
                val stops = day.items.map { it.place }
                val origin = "${stops.first().latitude},${stops.first().longitude}"
                val destination = "${stops.last().latitude},${stops.last().longitude}"
                val waypoints = stops.drop(1).dropLast(1)
                    .joinToString("|") { "${it.latitude},${it.longitude}" }

                append("&origin=").append(origin)
                append("&destination=").append(destination)
                if (waypoints.isNotEmpty()) {
                    append("&waypoints=").append(waypoints)
                }
            }
            targetLocation != null -> {
                append("&origin=").append("${userCoordinates.latitude},${userCoordinates.longitude}")
                append("&destination=").append("${targetLocation.latitude},${targetLocation.longitude}")
            }
            focusedPlace != null -> {
                append("&origin=").append("${userCoordinates.latitude},${userCoordinates.longitude}")
                append("&destination=").append("${focusedPlace.latitude},${focusedPlace.longitude}")
            }
        }
    }
}
