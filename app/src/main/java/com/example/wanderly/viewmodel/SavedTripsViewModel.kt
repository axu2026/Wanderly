package com.example.wanderly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.data.SavedTrip
import com.example.wanderly.data.SavedTripRepository
import com.example.wanderly.data.SavedTripSummary
import com.example.wanderly.data.model.ItineraryDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedTripsViewModel(application: Application) : AndroidViewModel(application) {

    val savedTrips: StateFlow<List<SavedTripSummary>> =
        SavedTripRepository.observeSummaries(application)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun saveCurrent(
        setup: TripSetupState,
        days: List<ItineraryDay>,
        onComplete: (Long) -> Unit = {},
    ) {
        if (days.isEmpty()) return
        viewModelScope.launch {
            val id = SavedTripRepository.save(getApplication(), setup, days)
            onComplete(id)
        }
    }

    fun load(id: Long, onLoaded: (SavedTrip?) -> Unit) {
        viewModelScope.launch {
            onLoaded(SavedTripRepository.load(getApplication(), id))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            SavedTripRepository.delete(getApplication(), id)
        }
    }
}
