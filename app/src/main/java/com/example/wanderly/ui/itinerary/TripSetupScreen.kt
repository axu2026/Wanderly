package com.example.wanderly.ui.itinerary

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wanderly.viewmodel.ItineraryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripSetupScreen(
    viewModel: ItineraryViewModel,
    onNavigateToItinerary: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var customPlaceInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Plan Your Journey", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        viewModel.generateItinerary(context)
                        onNavigateToItinerary()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = state.city.isNotBlank() && !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Generate Itinerary", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Destination Section
            SectionHeader(title = "Where are you going?")
            OutlinedTextField(
                value = state.city,
                onValueChange = { viewModel.updateCity(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. New York, Tokyo...") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Days Section
            SectionHeader(title = "How many days?")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Slider(
                    value = state.days.toFloat(),
                    onValueChange = { viewModel.updateDays(it.toInt()) },
                    valueRange = 1f..14f,
                    steps = 13,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${state.days} days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(64.dp)
                )
            }

            // Budget Section
            SectionHeader(title = "Budget per day")
            Column {
                Slider(
                    value = state.budgetPerDay.toFloat(),
                    onValueChange = { viewModel.updateBudget(it.toDouble()) },
                    valueRange = 20f..500f,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$20", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Selected: $${state.budgetPerDay.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text("$500", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Transport Section
            SectionHeader(title = "Transport Mode")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Walking", "Car", "Transit").forEach { mode ->
                    FilterChip(
                        selected = state.transportMode == mode,
                        onClick = { viewModel.updateTransport(mode) },
                        label = { Text(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interests Section
            SectionHeader(title = "Interests")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Food", "Culture", "Nightlife", "Nature", "Shopping").forEach { interest ->
                    FilterChip(
                        selected = state.selectedInterests.contains(interest),
                        onClick = { viewModel.toggleInterest(interest) },
                        label = { Text(interest) }
                    )
                }
            }

            // Custom Places Section
            SectionHeader(title = "Must-visit places")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customPlaceInput,
                    onValueChange = { customPlaceInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a specific place") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                IconButton(
                    onClick = {
                        if (customPlaceInput.isNotBlank()) {
                            viewModel.addCustomPlace(customPlaceInput)
                            customPlaceInput = ""
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            if (state.customPlaces.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.customPlaces.forEach { place ->
                        AssistChip(
                            onClick = { viewModel.removeCustomPlace(place) },
                            label = { Text(place) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}
