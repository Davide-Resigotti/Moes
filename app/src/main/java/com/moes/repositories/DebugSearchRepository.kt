package com.moes.repositories

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.search.result.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A simple implementation of the SearchRepository that does nothing.
 * This is useful for development until the real implementation is ready.
 */
class DebugSearchRepository : SearchRepository {
    override val searchResults: StateFlow<List<SearchResult>> = MutableStateFlow(emptyList())
    override val route: StateFlow<DirectionsRoute?> = MutableStateFlow(null)

    override fun onSearchQueryChanged(query: String) {
        // No-op
        println("Search query changed: $query")
    }

    override fun onSearchResultSelected(result: SearchResult) {
        // No-op
        println("Search result selected: $result")
    }
}
