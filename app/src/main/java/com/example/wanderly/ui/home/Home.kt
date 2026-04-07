package com.example.wanderly.ui.home

import android.location.Address
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.example.wanderly.ui.map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Home(coordinates: LatLng) {
    val context = LocalContext.current
    var address by rememberSaveable { mutableStateOf<Address?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(coordinates) {
        val lat = coordinates.latitude
        val lng = coordinates.longitude
        isLoading = true
        address = withContext(Dispatchers.IO) {
            reverseGeocode(context, lat, lng)
        }
        isLoading = false
    }

    if (isLoading) {
        Text("Loading")
    } else if (address != null) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp, 16.dp, 16.dp, 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            address?.let {
                Text("Welcome to ${getBestLocationName(it)}!")
            }
            Text("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV")
        }
    } else {
        Text("Location Unavailable!")
    }
}