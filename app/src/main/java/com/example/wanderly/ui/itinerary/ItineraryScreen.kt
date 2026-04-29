package com.example.wanderly.ui.itinerary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.Place
import com.example.wanderly.ui.itinerary.components.DayHeader
import com.example.wanderly.ui.itinerary.components.ItineraryItemCard
import com.example.wanderly.viewmodel.ItineraryViewModel
import com.example.wanderly.viewmodel.SavedTripsViewModel
import com.example.wanderly.viewmodel.TripSetupState
import com.example.wanderly.viewmodel.WeatherViewModel
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    viewModel: ItineraryViewModel,
    weatherViewModel: WeatherViewModel,
    savedTripsViewModel: SavedTripsViewModel,
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    var justSaved by remember { mutableStateOf(false) }

    LaunchedEffect(itinerary, state) {
        // Reset the "saved" indicator if the trip changes underneath us.
        justSaved = false
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = if (state.city.isNotBlank()) "Trip to ${state.city}" else "Your Trip",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (itinerary.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                savedTripsViewModel.saveCurrent(state, itinerary) {
                                    justSaved = true
                                    snackbarScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Trip saved to your profile",
                                            withDismissAction = true,
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (justSaved) Icons.Filled.Bookmark
                                    else Icons.Outlined.BookmarkAdd,
                                contentDescription = "Save trip",
                                tint = if (justSaved) MaterialTheme.colorScheme.primary
                                    else LocalContentColor.current,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
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
            else -> {
                val totalStops = itinerary.sumOf { it.items.size }
                val totalCost = itinerary.sumOf { it.totalCost }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "summary") {
                        TripSummaryCard(
                            state = state,
                            totalStops = totalStops,
                            totalCost = totalCost,
                        )
                    }
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
                                stopCount = day.items.size,
                                weather = weather.takeIf { day.dayNumber == 1 },
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
}

@Composable
private fun TripSummaryCard(
    state: TripSetupState,
    totalStops: Int,
    totalCost: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Your itinerary",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (state.city.isNotBlank()) state.city else "Your destination",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryStat(
                    icon = Icons.Outlined.CalendarMonth,
                    label = if (state.days == 1) "Day" else "Days",
                    value = "${state.days}",
                    modifier = Modifier.weight(1f),
                )
                SummaryStat(
                    icon = Icons.Outlined.Place,
                    label = "Stops",
                    value = "$totalStops",
                    modifier = Modifier.weight(1f),
                )
                SummaryStat(
                    icon = Icons.Outlined.AttachMoney,
                    label = "Est. cost",
                    value = "${totalCost.toInt()}",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Getting around: ${state.transportMode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            )
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
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (city.isNotBlank()) "Crafting your trip to $city" else "Crafting your trip",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pulling places, sorting routes, picking the good stuff…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FallbackBanner() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Showing curated suggestions — couldn't reach live places data right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
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
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
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
        FilledTonalButton(
            onClick = onPlanTrip,
            shape = RoundedCornerShape(50),
        ) {
            Text("Plan a trip")
        }
    }
}
