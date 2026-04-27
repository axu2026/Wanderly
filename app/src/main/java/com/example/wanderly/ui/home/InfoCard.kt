package com.example.wanderly.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wanderly.ui.Loading
import com.example.wanderly.viewmodel.InformationViewModel

@Composable
fun InfoCard(viewModel: InformationViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
        ) {
            val information by viewModel.information.collectAsState()
            val loading by viewModel.isLoading.collectAsState()

            if (loading) {
                Loading()
            } else if (information != null) {
                val data = information!!

                Text(
                    text = "Info",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = data.extract,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "No information available",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}