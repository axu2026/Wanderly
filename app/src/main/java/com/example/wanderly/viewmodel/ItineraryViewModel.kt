package com.example.wanderly.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.data.PlacesRepository
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.ItineraryItem
import com.example.wanderly.data.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID

data class TripSetupState(
    val city: String = "",
    val days: Int = 1,
    val budgetPerDay: Double = 100.0,
    val transportMode: String = "Walking",
    val selectedInterests: Set<String> = emptySet(),
    val customPlaces: List<String> = emptyList()
)

class ItineraryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TripSetupState())
    val uiState: StateFlow<TripSetupState> = _uiState.asStateFlow()

    private val _itinerary = MutableStateFlow<List<ItineraryDay>>(emptyList())
    val itinerary: StateFlow<List<ItineraryDay>> = _itinerary.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _usedFallback = MutableStateFlow(false)
    val usedFallback: StateFlow<Boolean> = _usedFallback.asStateFlow()

    fun updateCity(city: String) = _uiState.update { it.copy(city = city) }
    fun updateDays(days: Int) = _uiState.update { it.copy(days = days) }
    fun updateBudget(budget: Double) = _uiState.update { it.copy(budgetPerDay = budget) }
    fun updateTransport(mode: String) = _uiState.update { it.copy(transportMode = mode) }

    fun toggleInterest(interest: String) = _uiState.update { state ->
        val newInterests = if (state.selectedInterests.contains(interest)) {
            state.selectedInterests - interest
        } else {
            state.selectedInterests + interest
        }
        state.copy(selectedInterests = newInterests)
    }

    fun addCustomPlace(place: String) = _uiState.update {
        it.copy(customPlaces = it.customPlaces + place)
    }

    fun removeCustomPlace(place: String) = _uiState.update {
        it.copy(customPlaces = it.customPlaces - place)
    }

    fun loadSavedItinerary(setup: TripSetupState, days: List<ItineraryDay>) {
        _isGenerating.value = false
        _usedFallback.value = false
        _uiState.value = setup
        _itinerary.value = days
    }

    fun generateItinerary(context: Context) {
        if (_isGenerating.value) return
        val state = _uiState.value
        viewModelScope.launch {
            _isGenerating.value = true
            _itinerary.value = emptyList()
            _usedFallback.value = false

            val realPool = try {
                PlacesRepository.fetchPlacesForCity(
                    context = context,
                    city = state.city,
                    interests = state.selectedInterests,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            val pool = realPool ?: run {
                _usedFallback.value = true
                getMockPlaces()
            }

            _itinerary.value = ItineraryPlanner.plan(state, pool)
            _isGenerating.value = false
        }
    }

    private fun getMockPlaces() = listOf(
        // Breakfast
        Place("Russ & Daughters Cafe", "127 Orchard St", 40.7220, -73.9881, 22.0, 45, 4.7, "Food", mealType = "Breakfast"),
        Place("Clinton St. Baking Co.", "4 Clinton St", 40.7212, -73.9837, 18.0, 45, 4.6, "Food", mealType = "Breakfast"),
        Place("Balthazar Bakery", "80 Spring St", 40.7227, -73.9982, 16.0, 45, 4.5, "Food", mealType = "Breakfast"),
        // Lunch
        Place("Joe's Pizza", "7 Carmine St", 40.7305, -74.0021, 15.0, 45, 4.7, "Food", mealType = "Lunch"),
        Place("Shake Shack", "Madison Square Park", 40.7414, -73.9882, 14.0, 50, 4.5, "Food", mealType = "Lunch"),
        Place("Xi'an Famous Foods", "81 St Marks Pl", 40.7275, -73.9853, 13.0, 45, 4.6, "Food", mealType = "Lunch"),
        // Dinner
        Place("Katz's Delicatessen", "205 E Houston St", 40.7222, -73.9874, 30.0, 75, 4.8, "Food", mealType = "Dinner"),
        Place("Carbone", "181 Thompson St", 40.7280, -74.0008, 65.0, 90, 4.7, "Food", mealType = "Dinner"),
        Place("Lucali", "575 Henry St", 40.6831, -73.9985, 35.0, 80, 4.8, "Food", mealType = "Dinner"),
        // Activities — Nature
        Place("Central Park", "Manhattan, NY", 40.7850, -73.9682, 0.0, 120, 4.8, "Nature"),
        Place("High Line", "West Side", 40.7480, -74.0048, 0.0, 90, 4.7, "Nature"),
        Place("Brooklyn Bridge Park", "334 Furman St", 40.7024, -73.9969, 0.0, 90, 4.8, "Nature"),
        // Activities — Culture
        Place("Metropolitan Museum", "1000 5th Ave", 40.7794, -73.9632, 25.0, 180, 4.9, "Culture"),
        Place("Natural History Museum", "Central Park W", 40.7813, -73.9740, 23.0, 150, 4.8, "Culture"),
        Place("MoMA", "11 W 53rd St", 40.7614, -73.9776, 25.0, 150, 4.7, "Culture"),
        Place("9/11 Memorial", "180 Greenwich St", 40.7115, -74.0134, 28.0, 120, 4.9, "Culture"),
        // Activities — Shopping
        Place("Fifth Avenue", "Midtown", 40.7587, -73.9787, 0.0, 120, 4.6, "Shopping"),
        Place("SoHo", "Broadway & Spring", 40.7233, -74.0030, 0.0, 120, 4.6, "Shopping"),
        Place("Chelsea Market", "75 9th Ave", 40.7424, -74.0061, 20.0, 90, 4.6, "Shopping"),
        // Activities — Nightlife
        Place("Skyline Bar", "Times Square", 40.7580, -73.9855, 60.0, 90, 4.5, "Nightlife"),
        Place("Westlight Rooftop", "111 N 12th St", 40.7222, -73.9573, 45.0, 90, 4.6, "Nightlife"),
        Place("Comedy Cellar", "117 MacDougal St", 40.7301, -74.0008, 35.0, 90, 4.7, "Nightlife"),
    )
}

private object ItineraryPlanner {

    private enum class Slot { BREAKFAST, MORNING, LUNCH, AFTERNOON, EVENING, DINNER }

    private data class Anchor(val slot: Slot, val time: LocalTime)

    private val baseDay = listOf(
        Anchor(Slot.BREAKFAST, LocalTime.of(8, 0)),
        Anchor(Slot.MORNING, LocalTime.of(10, 0)),
        Anchor(Slot.LUNCH, LocalTime.of(12, 30)),
        Anchor(Slot.AFTERNOON, LocalTime.of(14, 30)),
        Anchor(Slot.DINNER, LocalTime.of(19, 0)),
    )

    private fun travelGapMinutes(transport: String) = when (transport) {
        "Car" -> 15
        "Transit" -> 20
        else -> 30 // Walking
    }

    fun plan(state: TripSetupState, pool: List<Place>): List<ItineraryDay> {
        val travelGap = travelGapMinutes(state.transportMode).toLong()
        val budget = state.budgetPerDay
        val nightlifeRequested = state.selectedInterests.contains("Nightlife")

        // Bucket the pool
        val breakfast = pool.filter { it.mealType == "Breakfast" && it.estimatedCost <= budget }.toMutableList()
        val lunch = pool.filter { it.mealType == "Lunch" && it.estimatedCost <= budget }.toMutableList()
        val dinner = pool.filter { it.mealType == "Dinner" && it.estimatedCost <= budget }.toMutableList()

        val allActivities = pool.filter { it.mealType == null }
        val activityCandidates = if (state.selectedInterests.isEmpty()) {
            allActivities
        } else {
            // Nightlife handled in evening slot, not as daytime activity
            allActivities.filter {
                it.category != null &&
                state.selectedInterests.contains(it.category) &&
                it.category != "Nightlife"
            }.ifEmpty { allActivities.filter { it.category != "Nightlife" } }
        }
        val activityPool = activityCandidates.filter { it.estimatedCost <= budget }.toMutableList()
        val nightlifePool = pool.filter { it.category == "Nightlife" && it.estimatedCost <= budget }.toMutableList()

        val customQueue = state.customPlaces.map { name ->
            Place(name, "Custom Location", 0.0, 0.0, 0.0, 90, 5.0, "Custom")
        }.toMutableList()

        val usedActivities = mutableSetOf<String>()
        val usedAll = mutableSetOf<String>()
        val days = mutableListOf<ItineraryDay>()

        fun popUnique(bucket: MutableList<Place>, refill: List<Place>): Place? {
            // Skip places already used across the whole trip
            while (true) {
                val candidate = popOrRecycle(bucket, refill) ?: return null
                if (usedAll.add(candidate.name.lowercase().trim())) return candidate
                if (bucket.isEmpty() && refill.all { usedAll.contains(it.name.lowercase().trim()) }) {
                    // Pool exhausted — accept a repeat rather than return null
                    return candidate
                }
            }
            @Suppress("UNREACHABLE_CODE") return null
        }

        for (dayNum in 1..state.days) {
            val items = mutableListOf<ItineraryItem>()
            var dayCost = 0.0
            var lastEnd: LocalTime? = null
            var morningCategory: String? = null

            val template = if (nightlifeRequested) {
                baseDay + Anchor(Slot.EVENING, LocalTime.of(21, 0))
            } else baseDay

            for (anchor in template) {
                val pick = when (anchor.slot) {
                    Slot.BREAKFAST -> popUnique(breakfast, pool.filter { it.mealType == "Breakfast" })
                    Slot.LUNCH -> popUnique(lunch, pool.filter { it.mealType == "Lunch" })
                    Slot.DINNER -> popUnique(dinner, pool.filter { it.mealType == "Dinner" })
                    Slot.MORNING -> {
                        val custom = customQueue.removeFirstOrNull()
                            ?.also { usedAll.add(it.name.lowercase().trim()) }
                        custom ?: pickActivity(activityPool, usedActivities, avoidCategory = null)
                            ?.also {
                                morningCategory = it.category
                                usedAll.add(it.name.lowercase().trim())
                            }
                    }
                    Slot.AFTERNOON -> pickActivity(activityPool, usedActivities, avoidCategory = morningCategory)
                        ?.also { usedAll.add(it.name.lowercase().trim()) }
                    Slot.EVENING -> popUnique(nightlifePool, pool.filter { it.category == "Nightlife" })
                } ?: continue

                if (dayCost + pick.estimatedCost > budget) {
                    // Meals are essential; activities are skippable
                    if (anchor.slot == Slot.MORNING || anchor.slot == Slot.AFTERNOON || anchor.slot == Slot.EVENING) {
                        continue
                    }
                }

                val start = lastEnd?.plusMinutes(travelGap)?.let { earliest ->
                    if (earliest.isAfter(anchor.time)) earliest else anchor.time
                } ?: anchor.time
                val end = start.plusMinutes(pick.averageDurationMinutes.toLong())

                items += ItineraryItem(
                    id = UUID.randomUUID().toString(),
                    place = pick,
                    startTime = start,
                    endTime = end,
                    cost = pick.estimatedCost,
                )
                dayCost += pick.estimatedCost
                lastEnd = end
                if (pick.mealType == null && pick.category != "Custom") {
                    usedActivities += pick.name
                }
            }

            days += ItineraryDay(dayNumber = dayNum, items = items, totalCost = dayCost)
        }

        return days
    }

    private fun popOrRecycle(bucket: MutableList<Place>, refill: List<Place>): Place? {
        if (bucket.isEmpty()) {
            if (refill.isEmpty()) return null
            bucket.addAll(refill.shuffled())
        }
        return bucket.removeFirstOrNull()
    }

    private fun pickActivity(
        pool: MutableList<Place>,
        used: Set<String>,
        avoidCategory: String?
    ): Place? {
        if (pool.isEmpty()) return null
        // Prefer unused, different-category-from-morning, but degrade gracefully
        val candidates = pool.toList()
        val firstChoice = candidates.firstOrNull { it.name !in used && it.category != avoidCategory }
        val secondChoice = candidates.firstOrNull { it.name !in used }
        val thirdChoice = candidates.firstOrNull { it.category != avoidCategory }
        val pick = firstChoice ?: secondChoice ?: thirdChoice ?: candidates.first()
        pool.remove(pick)
        return pick
    }
}
