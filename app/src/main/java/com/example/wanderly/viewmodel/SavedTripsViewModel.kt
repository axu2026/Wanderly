package com.example.wanderly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.data.SavedTrip
import com.example.wanderly.data.SavedTripRepository
import com.example.wanderly.data.SavedTripSummary
import com.example.wanderly.data.model.ItineraryDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SavedTripsViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentUserId = MutableStateFlow<Long?>(null)

    val savedTrips: StateFlow<List<SavedTripSummary>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else SavedTripRepository.observeSummaries(getApplication(), userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setCurrentUser(userId: Long?) {
        _currentUserId.value = userId
    }

    fun saveCurrent(
        setup: TripSetupState,
        days: List<ItineraryDay>,
        onComplete: (Long) -> Unit = {},
    ) {
        if (days.isEmpty()) return
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            val id = SavedTripRepository.save(getApplication(), userId, setup, days)
            onComplete(id)
        }
    }

    fun load(id: Long, onLoaded: (SavedTrip?) -> Unit) {
        val userId = _currentUserId.value
        if (userId == null) {
            onLoaded(null)
            return
        }
        viewModelScope.launch {
            onLoaded(SavedTripRepository.load(getApplication(), id, userId))
        }
    }

    fun delete(id: Long) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            SavedTripRepository.delete(getApplication(), id, userId)
        }
    }
}
