package com.example.wanderly.data

import android.content.Context
import com.example.wanderly.data.model.ItineraryDay
import com.example.wanderly.data.model.ItineraryItem
import com.example.wanderly.data.model.Place
import com.example.wanderly.local.DatabaseProvider
import com.example.wanderly.local.savedtrips.SavedTripEntity
import com.example.wanderly.viewmodel.TripSetupState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

data class SavedTrip(
    val id: Long,
    val savedAt: Long,
    val setup: TripSetupState,
    val days: List<ItineraryDay>,
)

data class SavedTripSummary(
    val id: Long,
    val city: String,
    val days: Int,
    val stopCount: Int,
    val totalCost: Double,
    val transportMode: String,
    val savedAt: Long,
)

object SavedTripRepository {
    private val gson = Gson()

    fun observeSummaries(context: Context): Flow<List<SavedTripSummary>> {
        return DatabaseProvider.getDatabase(context)
            .savedTripDao()
            .observeAll()
            .map { entities -> entities.map { it.toSummary() } }
    }

    suspend fun save(
        context: Context,
        setup: TripSetupState,
        days: List<ItineraryDay>,
    ): Long {
        val payload = TripPayloadDto(
            setup = setup.toDto(),
            days = days.map { it.toDto() },
        )
        val entity = SavedTripEntity(
            city = setup.city.ifBlank { "Untitled trip" },
            days = setup.days,
            stopCount = days.sumOf { it.items.size },
            totalCost = days.sumOf { it.totalCost },
            transportMode = setup.transportMode,
            savedAt = System.currentTimeMillis(),
            payloadJson = gson.toJson(payload),
        )
        return DatabaseProvider.getDatabase(context).savedTripDao().insert(entity)
    }

    suspend fun load(context: Context, id: Long): SavedTrip? {
        val entity = DatabaseProvider.getDatabase(context).savedTripDao().get(id) ?: return null
        val payload = gson.fromJson(entity.payloadJson, TripPayloadDto::class.java)
        return SavedTrip(
            id = entity.id,
            savedAt = entity.savedAt,
            setup = payload.setup.toDomain(),
            days = payload.days.map { it.toDomain() },
        )
    }

    suspend fun delete(context: Context, id: Long) {
        DatabaseProvider.getDatabase(context).savedTripDao().delete(id)
    }

    private fun SavedTripEntity.toSummary() = SavedTripSummary(
        id = id,
        city = city,
        days = days,
        stopCount = stopCount,
        totalCost = totalCost,
        transportMode = transportMode,
        savedAt = savedAt,
    )
}

private data class TripPayloadDto(
    val setup: TripSetupDto,
    val days: List<DayDto>,
)

private data class TripSetupDto(
    val city: String,
    val days: Int,
    val budgetPerDay: Double,
    val transportMode: String,
    val selectedInterests: List<String>,
    val customPlaces: List<String>,
)

private data class DayDto(
    val dayNumber: Int,
    val totalCost: Double,
    val items: List<ItemDto>,
)

private data class ItemDto(
    val id: String,
    val place: PlaceDto,
    val startTime: String,
    val endTime: String,
    val cost: Double,
    val isCompleted: Boolean,
)

private data class PlaceDto(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val estimatedCost: Double,
    val averageDurationMinutes: Int,
    val rating: Double?,
    val category: String?,
    val mealType: String?,
)

private fun TripSetupState.toDto() = TripSetupDto(
    city = city,
    days = days,
    budgetPerDay = budgetPerDay,
    transportMode = transportMode,
    selectedInterests = selectedInterests.toList(),
    customPlaces = customPlaces,
)

private fun TripSetupDto.toDomain() = TripSetupState(
    city = city,
    days = days,
    budgetPerDay = budgetPerDay,
    transportMode = transportMode,
    selectedInterests = selectedInterests.toSet(),
    customPlaces = customPlaces,
)

private fun ItineraryDay.toDto() = DayDto(
    dayNumber = dayNumber,
    totalCost = totalCost,
    items = items.map { it.toDto() },
)

private fun DayDto.toDomain() = ItineraryDay(
    dayNumber = dayNumber,
    totalCost = totalCost,
    items = items.map { it.toDomain() },
)

private fun ItineraryItem.toDto() = ItemDto(
    id = id,
    place = place.toDto(),
    startTime = startTime.toString(),
    endTime = endTime.toString(),
    cost = cost,
    isCompleted = isCompleted,
)

private fun ItemDto.toDomain() = ItineraryItem(
    id = id,
    place = place.toDomain(),
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
    cost = cost,
    isCompleted = isCompleted,
)

private fun Place.toDto() = PlaceDto(
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    estimatedCost = estimatedCost,
    averageDurationMinutes = averageDurationMinutes,
    rating = rating,
    category = category,
    mealType = mealType,
)

private fun PlaceDto.toDomain() = Place(
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    estimatedCost = estimatedCost,
    averageDurationMinutes = averageDurationMinutes,
    rating = rating,
    category = category,
    mealType = mealType,
)
