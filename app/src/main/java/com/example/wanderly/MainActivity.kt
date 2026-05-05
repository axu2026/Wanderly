package com.example.wanderly

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.Place
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.ui.auth.LoginScreen
import com.example.wanderly.ui.auth.SignupScreen
import com.example.wanderly.ui.onboarding.OnboardingScreen
import com.example.wanderly.ui.theme.WanderlyTheme
import com.example.wanderly.viewmodel.LocationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wanderly.ui.Loading
import com.example.wanderly.ui.map.*
import com.example.wanderly.ui.home.*
import com.example.wanderly.ui.profile.*
import com.example.wanderly.viewmodel.*
import com.google.android.gms.maps.model.LatLng

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WanderlyTheme {
                WanderlyApp()
            }
        }
    }
}

@Composable
fun WanderlyApp(
    locationViewModel: LocationViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()

    when {
        !authState.sessionRestored -> Loading()
        !authState.hasSeenOnboarding -> OnboardingScreen(
            onFinish = { authViewModel.completeOnboarding() }
        )
        authState.currentUser == null -> AuthFlow(authViewModel)
        else -> MainApp(locationViewModel, authViewModel)
    }
}

@Composable
private fun AuthFlow(authViewModel: AuthViewModel) {
    var showSignup by rememberSaveable { mutableStateOf(false) }
    if (showSignup) {
        SignupScreen(
            authViewModel = authViewModel,
            onNavigateToLogin = { showSignup = false },
        )
    } else {
        LoginScreen(
            authViewModel = authViewModel,
            onNavigateToSignup = { showSignup = true },
        )
    }
}

@Composable
private fun MainApp(
    locationViewModel: LocationViewModel,
    authViewModel: AuthViewModel,
) {
    val state by locationViewModel.state.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    val homeViewModel: HomeViewModel = viewModel()
    val weatherViewModel: WeatherViewModel = viewModel()
    val itineraryViewModel: ItineraryViewModel = viewModel()
    val placesViewModel: PlacesViewModel = viewModel()
    val informationViewModel: InformationViewModel = viewModel()
    val savedTripsViewModel: SavedTripsViewModel = viewModel()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var showItineraryDetails by rememberSaveable { mutableStateOf(false) }
    var focusedPlace by remember { mutableStateOf<Place?>(null) }
    var focusedDay by remember { mutableStateOf<ItineraryDay?>(null) }
    val tripState by itineraryViewModel.uiState.collectAsState()

    var targetPlace by remember { mutableStateOf<PlaceResult?>(null) }
    var searchedDestination by remember { mutableStateOf<LatLng?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                locationViewModel.startLocationUpdates()
            } catch (e: SecurityException) {
                Log.e("Location", "Security Exception: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!locationViewModel.hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            try {
                locationViewModel.startLocationUpdates()
            } catch (e: SecurityException) {
                Log.e("Location", "Security Exception: ${e.message}")
            }
        }
    }

    if (state.latitude != null && state.longitude != null) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = { Icon(it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = {
                            currentDestination = it
                            if (it != AppDestinations.MAP) {
                                targetPlace = null
                                focusedPlace = null
                                focusedDay = null
                                searchedDestination = null
                            }
                        }
                    )
                }
            }
        ) {
            val userCoordinates = LatLng(state.latitude!!, state.longitude!!)

            when (currentDestination) {
                AppDestinations.HOME -> Home(
                    homeViewModel,
                    weatherViewModel,
                    placesViewModel,
                    informationViewModel,
                    userCoordinates,
                    onPlaceClick = { place ->
                        targetPlace = place
                        focusedPlace = null
                        focusedDay = null
                        searchedDestination = null
                        currentDestination = AppDestinations.MAP
                    },
                    onShowRoute = { place ->
                        searchedDestination = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                        targetPlace = place
                        focusedPlace = null
                        focusedDay = null
                        currentDestination = AppDestinations.MAP
                    }
                )
                AppDestinations.ITINERARY -> {
                    if (showItineraryDetails) {
                        com.example.wanderly.ui.itinerary.ItineraryScreen(
                            viewModel = itineraryViewModel,
                            weatherViewModel = weatherViewModel,
                            savedTripsViewModel = savedTripsViewModel,
                            onBack = { showItineraryDetails = false },
                            onPlaceClick = { place ->
                                focusedPlace = place
                                focusedDay = null
                                targetPlace = null
                                searchedDestination = null
                                currentDestination = AppDestinations.MAP
                            },
                            onDayClick = { day ->
                                focusedDay = day
                                focusedPlace = null
                                targetPlace = null
                                searchedDestination = null
                                currentDestination = AppDestinations.MAP
                            }
                        )
                    } else {
                        com.example.wanderly.ui.itinerary.TripSetupScreen(
                            viewModel = itineraryViewModel,
                            onNavigateToItinerary = { showItineraryDetails = true }
                        )
                    }
                }
                AppDestinations.PROFILE -> Profile(
                    user = authState.currentUser,
                    address = homeViewModel.address,
                    savedTripsViewModel = savedTripsViewModel,
                    onLoadTrip = { tripId ->
                        savedTripsViewModel.load(tripId) { saved ->
                            if (saved != null) {
                                itineraryViewModel.loadSavedItinerary(saved.setup, saved.days)
                                showItineraryDetails = true
                                currentDestination = AppDestinations.ITINERARY
                            }
                        }
                    },
                    onLogout = { authViewModel.logout() },
                )
                AppDestinations.MAP -> Map(
                    userCoordinates = userCoordinates,
                    targetPlace = targetPlace,
                    focusedPlace = focusedPlace,
                    focusedDay = focusedDay,
                    searchedDestination = searchedDestination,
                    transportMode = tripState.transportMode,
                    placesViewModel = placesViewModel,
                    informationViewModel = informationViewModel,
                    onClearFocus = {
                        targetPlace = null
                        focusedPlace = null
                        focusedDay = null
                        searchedDestination = null
                    }
                )
            }
        }
    } else {
        Loading()
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    ITINERARY("Itinerary", Icons.Default.DateRange),
    MAP("Map", Icons.Default.LocationOn),
    PROFILE("Profile", Icons.Default.AccountBox),
}
