package com.example.wanderly.ui.itinerary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wanderly.viewmodel.ItineraryViewModel

private const val DEFAULT_BUDGET_MAX = 1000

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripSetupScreen(
    viewModel: ItineraryViewModel,
    onNavigateToItinerary: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var customPlaceInput by remember { mutableStateOf("") }
    var budgetMax by remember { mutableIntStateOf(DEFAULT_BUDGET_MAX) }

    // Keep slider max in sync if a high value is restored from state.
    LaunchedEffect(state.budgetPerDay) {
        if (state.budgetPerDay.toInt() > budgetMax) budgetMax = state.budgetPerDay.toInt()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Plan your journey",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
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
                    shape = RoundedCornerShape(50),
                    enabled = state.city.isNotBlank() && !isGenerating,
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate itinerary",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeroIntroCard()

            SettingCard(icon = Icons.Outlined.LocationOn, title = "Where to?") {
                OutlinedTextField(
                    value = state.city,
                    onValueChange = { viewModel.updateCity(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. New York, Tokyo, Lisbon…") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }

            SettingCard(icon = Icons.Outlined.CalendarMonth, title = "How long?") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Slider(
                        value = state.days.toFloat(),
                        onValueChange = { viewModel.updateDays(it.toInt()) },
                        valueRange = 1f..14f,
                        steps = 12,
                        modifier = Modifier.weight(1f),
                    )
                    ValueChip(
                        text = "${state.days} ${if (state.days == 1) "day" else "days"}",
                    )
                }
            }

            SettingCard(icon = Icons.Outlined.AttachMoney, title = "Budget per day") {
                Text(
                    text = "$${state.budgetPerDay.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Slider(
                    value = state.budgetPerDay.toFloat().coerceAtMost(budgetMax.toFloat()),
                    onValueChange = { viewModel.updateBudget(it.toDouble()) },
                    valueRange = 20f..budgetMax.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$20",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$$budgetMax",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                CustomBudgetField(
                    currentBudget = state.budgetPerDay.toInt(),
                    onApply = { typed ->
                        val clamped = typed.coerceAtLeast(1)
                        if (clamped > budgetMax) budgetMax = clamped
                        viewModel.updateBudget(clamped.toDouble())
                    },
                )
            }

            SettingCard(icon = Icons.Outlined.DirectionsCar, title = "Getting around") {
                val transportOptions = listOf(
                    Triple("Walking", Icons.AutoMirrored.Outlined.DirectionsWalk, "Walking"),
                    Triple("Car", Icons.Outlined.DirectionsCar, "Car"),
                    Triple("Transit", Icons.Outlined.DirectionsBus, "Transit"),
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    transportOptions.forEachIndexed { index, (mode, icon, label) ->
                        SegmentedButton(
                            selected = state.transportMode == mode,
                            onClick = { viewModel.updateTransport(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = transportOptions.size,
                            ),
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            label = { Text(label) },
                        )
                    }
                }
            }

            SettingCard(icon = Icons.Outlined.Interests, title = "What do you love?") {
                val interests = listOf(
                    "Food" to Icons.Outlined.Restaurant,
                    "Culture" to Icons.Outlined.Museum,
                    "Nightlife" to Icons.Outlined.LocalBar,
                    "Nature" to Icons.Outlined.Forest,
                    "Shopping" to Icons.Outlined.ShoppingBag,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    interests.forEach { (interest, icon) ->
                        FilterChip(
                            selected = state.selectedInterests.contains(interest),
                            onClick = { viewModel.toggleInterest(interest) },
                            label = { Text(interest) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }
            }

            SettingCard(icon = Icons.Outlined.PushPin, title = "Must-visit spots") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = customPlaceInput,
                        onValueChange = { customPlaceInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. Eiffel Tower") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    FilledIconButton(
                        onClick = {
                            if (customPlaceInput.isNotBlank()) {
                                viewModel.addCustomPlace(customPlaceInput.trim())
                                customPlaceInput = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add place")
                    }
                }

                if (state.customPlaces.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.customPlaces.forEach { place ->
                            InputChip(
                                selected = false,
                                onClick = { viewModel.removeCustomPlace(place) },
                                label = { Text(place) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HeroIntroCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Tell us about your trip",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "We'll craft a day-by-day plan in seconds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun SettingCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun ValueChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomBudgetField(
    currentBudget: Int,
    onApply: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    if (!expanded) {
        TextButton(
            onClick = {
                input = currentBudget.toString()
                expanded = true
            },
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Set custom amount")
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { typed -> input = typed.filter { it.isDigit() }.take(7) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                label = { Text("Custom amount") },
                prefix = { Text("$") },
                suffix = { Text("/day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            FilledTonalButton(
                onClick = {
                    val typed = input.toIntOrNull()
                    if (typed != null && typed > 0) {
                        onApply(typed)
                        expanded = false
                    }
                },
                shape = RoundedCornerShape(14.dp),
                enabled = input.toIntOrNull()?.let { it > 0 } == true,
            ) {
                Text("Apply")
            }
        }
    }
}
