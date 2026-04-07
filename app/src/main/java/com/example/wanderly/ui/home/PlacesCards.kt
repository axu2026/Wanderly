package com.example.wanderly.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wanderly.viewmodel.PlacesViewModel
import com.example.wanderly.ui.Loading
import androidx.compose.foundation.lazy.items
import com.example.wanderly.api.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesCards(viewModel: PlacesViewModel) {
    val places by viewModel.places.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Loading()
    } else if (places.isEmpty()) {
        Text("No Places Nearby!")
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(places) { place ->
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(150.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            place.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            place.categories.firstOrNull()?.name ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                        place.distance?.let {
                            Text(formatDistance(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}