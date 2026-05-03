package com.example.wanderly.ui.home

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wanderly.BuildConfig
import com.example.wanderly.api.PlaceResult
import com.example.wanderly.ui.Loading
import com.google.android.gms.maps.model.LatLng

private data class PlaceWithDistance(
    val place: PlaceResult,
    val distance: Float
)

@Composable
fun PlacesCards(
    title: String,
    places: List<PlaceResult>,
    userLocation: LatLng,
    isLoading: Boolean = false,
    onPlaceClick: (PlaceResult) -> Unit = {}
) {
    val sortedWithDistance = remember(places, userLocation) {
        places.map { place ->
            val results = FloatArray(1)
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                place.geometry.location.lat, place.geometry.location.lng,
                results
            )
            PlaceWithDistance(place, results[0])
        }.sortedBy { it.distance }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        AdaptiveTitle(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Loading()
            }
        } else if (sortedWithDistance.isEmpty()) {
            Text(
                "No places found nearby.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedWithDistance) { item ->
                    PlaceCard(
                        place = item.place, 
                        distance = item.distance,
                        onClick = {
                            onPlaceClick(item.place)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceCard(
    place: PlaceResult, 
    distance: Float,
    onClick: () -> Unit
) {
    val photoReference = place.photos?.firstOrNull()?.photo_reference
    val imageUrl = if (photoReference != null) {
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$photoReference&key=${BuildConfig.MAPS_API_KEY}"
    } else null

    val distanceText = if (distance < 1000) {
        "${distance.toInt()}m"
    } else {
        String.format("%.1fkm", distance / 1000)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .width(260.dp)
            .height(170.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.vicinity ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
