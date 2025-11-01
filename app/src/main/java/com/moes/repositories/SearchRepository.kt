package com.moes.repositories

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.search.result.SearchResult
import kotlinx.coroutines.flow.StateFlow

interface SearchRepository {
    val searchResults: StateFlow<List<SearchResult>>
    val route: StateFlow<DirectionsRoute?>

    fun onSearchQueryChanged(query: String)
    fun onSearchResultSelected(result: SearchResult)
}
