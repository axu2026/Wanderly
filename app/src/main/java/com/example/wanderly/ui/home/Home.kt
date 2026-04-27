package com.example.wanderly.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.example.wanderly.ui.map.*
import com.example.wanderly.viewmodel.*
import com.example.wanderly.ui.Loading

// the home screen composable
@Composable
fun Home(
    viewModel: HomeViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    placesViewModel: PlacesViewModel = viewModel(),
    coordinates: LatLng
) {
    val context = LocalContext.current
    val address = viewModel.address
    val isLoading = viewModel.isLoading
    val scrollState = rememberScrollState()

    // fetch data when coordinates change
    LaunchedEffect(coordinates) {
        viewModel.geocodeAddressIfNeeded(context, coordinates)
        weatherViewModel.fetchWeather(coordinates)
        placesViewModel.fetchPlaces(coordinates)
    }

    // show loading screen while fetching data
    if (isLoading) {
        Loading()
    } else if (address != null) { // show home screen when data is available
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "${getBestLocationName(address)}!",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAddress(address),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            WeatherCard(weatherViewModel)
            Spacer(modifier = Modifier.height(8.dp))
            PlacesCards(placesViewModel)
        }
    } else {
        Loading()
    }
}