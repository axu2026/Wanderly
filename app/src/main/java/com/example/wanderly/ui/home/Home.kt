package com.example.wanderly.ui.home

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    informationViewModel: InformationViewModel = viewModel(),
    coordinates: LatLng
) {
    val context = LocalContext.current
    val address = viewModel.address
    val isLoading = viewModel.isLoading
    val scrollState = rememberScrollState()
    val searchQuery by placesViewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current

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

    // show loading screen while fetching data
    if (isLoading || address == null) {
        Loading()
    } else {
        val bestLocationName = getBestLocationName(address) ?: "Unknown Location"
        
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
                text = "$bestLocationName!",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAddress(address),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            PlacesCards(placesViewModel)
        }
    }
}