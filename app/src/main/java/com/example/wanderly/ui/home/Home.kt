package com.example.wanderly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.ui.components.PlaceDetailSheet
import com.example.wanderly.ui.map.*
import com.example.wanderly.viewmodel.*

// the home screen composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    viewModel: HomeViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    placesViewModel: PlacesViewModel = viewModel(),
    informationViewModel: InformationViewModel = viewModel(),
    coordinates: LatLng,
    onPlaceClick: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val address = viewModel.address
    val scrollState = rememberScrollState()

    // Bottom sheet state
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // places state
    val importantPlaces by placesViewModel.importantPlaces.collectAsState()
    val importantIsLoading by placesViewModel.importantIsLoading.collectAsState()

    // background
    val backgroundImageUrl = viewModel.backgroundImageUrl

    // fetch data when coordinates change
    LaunchedEffect(coordinates) {
        viewModel.geocodeAddressIfNeeded(context, coordinates)
        weatherViewModel.fetchWeather(coordinates)
        placesViewModel.fetchImportantPlaces(coordinates)
    }

    // fetch information only when address is available
    LaunchedEffect(address) {
        if (address != null) {
            informationViewModel.fetchLocationInformation(address)
        }
    }

    // fetch information for important places
    LaunchedEffect(importantPlaces) {
        if (importantPlaces.isNotEmpty()) {
            informationViewModel.fetchImportantPlacesInformation(importantPlaces)
        }
    }

    // Common text shadow for better readability on images
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(0f, 4f),
        blurRadius = 10f
    )

    // Using surface for better consistency with other screens
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // 1. Background Image (Top Half only)
        if (backgroundImageUrl != null) {
            AsyncImage(
                model = backgroundImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().fillMaxSize(0.5f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
        }

        // 2. Refined Gradient Scrim (Fades to surface by midpoint)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.65f),
                        0.2f to Color.Black.copy(alpha = 0.3f),
                        0.5f to MaterialTheme.colorScheme.surface,
                        1.0f to MaterialTheme.colorScheme.surface
                    )
                )
        )

        // 3. Content
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // display welcome, address, city
            if (address != null) {
                val bestLocationName = getBestLocationName(address) ?: "Unknown Location"
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.headlineMedium.copy(shadow = textShadow),
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "$bestLocationName!",
                    style = MaterialTheme.typography.displayLarge.copy(shadow = textShadow),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = formatAddress(address),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge.copy(shadow = textShadow),
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else {
                Text(
                    text = "Locating...",
                    style = MaterialTheme.typography.headlineMedium.copy(shadow = textShadow),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // weather info
                WeatherCard(weatherViewModel)

                // general city info
                val info by informationViewModel.locationInfo.collectAsState()
                val infoIsLoading by informationViewModel.locationInfoIsLoading.collectAsState()
                InfoCard(loading = infoIsLoading, information = info)

                // nearby sites info
                val importantPlacesInfo by informationViewModel.importantPlacesInfo.collectAsState()
                val importantPlacesInfoIsLoading by informationViewModel.importantPlacesInfoIsLoading.collectAsState()
                if (importantPlacesInfo.isNotEmpty() || importantPlacesInfoIsLoading) {
                    AdaptiveTitle(
                        text = "Nearby Info",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    InfoCards(
                        information = importantPlacesInfo.filterNotNull(),
                        isLoading = importantPlacesInfoIsLoading
                    )
                }

                val isImportantLoading by placesViewModel.importantIsLoading.collectAsState()

                // Featured/Important Spots Section
                if (importantPlaces.isNotEmpty()) {
                    PlacesCards(
                        title = "Featured Spots",
                        places = importantPlaces,
                        isLoading = isImportantLoading,
                        userLocation = coordinates,
                        onPlaceClick = {
                            selectedPlace = it
                            informationViewModel.fetchPlaceInformation(it)
                            showBottomSheet = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // bottom modal for place details
        if (showBottomSheet && selectedPlace != null) {
            val wikiSummary by informationViewModel.selectedPlaceInfo.collectAsState()
            val wikiLoading by informationViewModel.selectedPlaceInfoIsLoading.collectAsState()

            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                PlaceDetailSheet(
                    place = selectedPlace!!,
                    wikiSummary = wikiSummary,
                    isLoading = wikiLoading,
                    onViewOnMap = {
                        showBottomSheet = false
                        onPlaceClick(LatLng(it.geometry.location.lat, it.geometry.location.lng))
                    }
                )
            }
        }
    }
}
