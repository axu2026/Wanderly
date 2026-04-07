package com.example.wanderly.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wanderly.viewmodel.WeatherViewModel

@Composable
fun WeatherCard(viewModel: WeatherViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        val weather by viewModel.weather.collectAsState()
        val loading by viewModel.isLoading.collectAsState()

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (weather != null) {
            val data = weather!!

            Column(
                modifier = Modifier
                    .padding(24.dp),
            ) {
                Text(
                    text = "Weather",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${data.main.temp}°C",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = data.weather.firstOrNull()?.description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text("Weather Unavailable!")
        }
    }
}