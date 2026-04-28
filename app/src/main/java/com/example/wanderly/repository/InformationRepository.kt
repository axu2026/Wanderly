package com.example.wanderly.repository

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
        val page = geoResult.query.geosearch.firstOrNull()
            ?: return null

        return wikiApi.getSummary(page.title)
    }
}