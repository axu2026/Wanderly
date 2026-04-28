package com.example.wanderly.repository

import android.util.Log
import com.example.wanderly.api.SummaryResponse
import com.example.wanderly.api.WikiApi
import com.example.wanderly.api.WikiSummary
import com.example.wanderly.api.GeoWikiApi

class InformationRepository(
    private val wikiApi: WikiApi,
    private val geoWikiApi: GeoWikiApi
) {
    suspend fun getSummary(title: String): WikiSummary {
        val safeTitle = title.replace(" ", "_")
        return wikiApi.getSummary(safeTitle)
    }

    suspend fun getNearbySummary(lat: Double, lon: Double): WikiSummary? {
        val coords = "$lat|$lon"
        val geoResult = geoWikiApi.geoSearch(coords)

        for (page in geoResult.query.geosearch) {
            try {
                val summary = wikiApi.getSummary(page.title)

                // Skip disambiguation or empty summaries
                if (summary.type != "disambiguation" && !summary.extract.isNullOrBlank()) {
                    return summary
                }
            } catch (e: Exception) {
                Log.d("InformationRepository", "Unable to fetch summary for page: ${page.title}")
            }
        }

        return null
    }
}