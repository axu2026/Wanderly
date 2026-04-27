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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
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

// the wanderly app main composable
@Composable
fun WanderlyApp(
    viewModel: LocationViewModel = viewModel()
) {
    // location viewModel
    val state by viewModel.state.collectAsState()

    // create all the view models necessary for state
    val homeViewModel: HomeViewModel = viewModel()
    val weatherViewModel: WeatherViewModel = viewModel()
    val placesViewModel: PlacesViewModel = viewModel()

    // mutable state for navigation
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // permission launcher for location
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                viewModel.startLocationUpdates()
            } catch (e: SecurityException) {
                Log.e("Location", "Security Exception: ${e.message}")
            }
        }
    }

    // request location permission on launch
    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            try {
                viewModel.startLocationUpdates()
            } catch (e: SecurityException) {
                Log.e("Location", "Security Exception: ${e.message}")
            }
        }
    }

    // run the app if location is available
    if (state.latitude != null && state.longitude != null) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.label
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            // convert state to latlng for each view
            val coordinates = LatLng(state.latitude!!, state.longitude!!)

            when (currentDestination) {
                AppDestinations.HOME -> Home(homeViewModel, weatherViewModel, placesViewModel, coordinates)
                AppDestinations.PROFILE -> Profile()
                AppDestinations.MAP -> Map(coordinates)
            }
        }
    } else {
        Loading()
    }
}

// for navigation
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    MAP("Map", Icons.Default.LocationOn),
    PROFILE("Profile", Icons.Default.AccountBox),
}