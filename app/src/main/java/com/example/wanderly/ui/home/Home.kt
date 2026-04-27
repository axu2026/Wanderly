package com.example.wanderly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.example.wanderly.ui.map.*
import com.example.wanderly.viewmodel.*

// the home screen composable
@Composable
fun Home(
    viewModel: HomeViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    placesViewModel: PlacesViewModel = viewModel(),
    informationViewModel: InformationViewModel = viewModel(),
    coordinates: LatLng
) {
    val context = LocalContext.current
    val address = viewModel.address
    val scrollState = rememberScrollState()
    val searchQuery by placesViewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current
    val backgroundImageUrl = viewModel.backgroundImageUrl

    // fetch data when coordinates change
    LaunchedEffect(coordinates) {
        viewModel.geocodeAddressIfNeeded(context, coordinates)
        weatherViewModel.fetchWeather(coordinates)
        placesViewModel.fetchPlaces(coordinates)
    }

    // fetch information only when address is available
    LaunchedEffect(address) {
        if (address != null) {
            informationViewModel.fetchInformation(address)
        }
    }

    // Common text shadow for better readability on images
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(0f, 4f),
        blurRadius = 10f
    )

    // Using surfaceContainerLow for better contrast with cards
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
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

        // 2. Refined Gradient Scrim (Fades to surfaceContainerLow by midpoint)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.65f),
                        0.2f to Color.Black.copy(alpha = 0.3f),
                        0.5f to MaterialTheme.colorScheme.surfaceContainerLow,
                        1.0f to MaterialTheme.colorScheme.surfaceContainerLow
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
                Spacer(modifier = Modifier.height(8.dp))
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

            Spacer(modifier = Modifier.height(48.dp))
            
            WeatherCard(weatherViewModel)
            InfoCard(informationViewModel)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { placesViewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for places (e.g. cafe, park)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        placesViewModel.fetchPlaces(coordinates)
                        focusManager.clearFocus()
                    }
                ),
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PlacesCards(placesViewModel)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
