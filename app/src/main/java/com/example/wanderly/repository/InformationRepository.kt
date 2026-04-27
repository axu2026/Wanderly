package com.example.wanderly.repository

import com.example.wanderly.api.WikiApi
import com.example.wanderly.api.WikiSummary

class InformationRepository(
    private val wikiApi: WikiApi
) {
    suspend fun getSummary(title: String): WikiSummary {
        val safeTitle = title.replace(" ", "_")
        return wikiApi.getSummary(safeTitle)
    }
}