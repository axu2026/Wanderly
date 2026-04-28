package com.example.wanderly.ui.itinerary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.Place
import com.example.wanderly.ui.itinerary.components.DayHeader
import com.example.wanderly.ui.itinerary.components.ItineraryItemCard
import com.example.wanderly.viewmodel.ItineraryViewModel
import com.example.wanderly.viewmodel.WeatherViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    viewModel: ItineraryViewModel,
    weatherViewModel: WeatherViewModel,
    onBack: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onDayClick: (ItineraryDay) -> Unit,
) {
    val itinerary by viewModel.itinerary.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val weather by weatherViewModel.weather.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val usedFallback by viewModel.usedFallback.collectAsState()

    val firstPlace = itinerary.firstOrNull()?.items?.firstOrNull()?.place
    LaunchedEffect(firstPlace?.name) {
        firstPlace?.let { weatherViewModel.fetchWeather(LatLng(it.latitude, it.longitude)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.city.isNotBlank()) "Trip to ${state.city}" else "Your Trip",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "${state.days} Days • ${state.transportMode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        when {
            isGenerating -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                city = state.city,
            )
            itinerary.isEmpty() -> EmptyItineraryState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onPlanTrip = onBack,
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (usedFallback) {
                    item(key = "fallback-banner") {
                        FallbackBanner()
                    }
                }
                itinerary.forEach { day ->
                    item(key = "day-${day.dayNumber}") {
                        DayHeader(
                            dayNumber = day.dayNumber,
                            totalCost = day.totalCost,
                            weather = weather,
                            onClick = { onDayClick(day) },
                        )
                    }
                    items(day.items, key = { it.id }) { item ->
                        ItineraryItemCard(
                            item = item,
                            onClick = { onPlaceClick(item.place) },
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier, city: String) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (city.isNotBlank()) "Crafting your trip to $city…" else "Crafting your trip…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FallbackBanner() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Showing curated suggestions — couldn't reach live places data right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun EmptyItineraryState(
    modifier: Modifier = Modifier,
    onPlanTrip: () -> Unit,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No itinerary yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tell us where you're heading and what you love. We'll build a day-by-day plan in seconds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPlanTrip) {
            Text("Plan a Trip")
        }
    }
}
