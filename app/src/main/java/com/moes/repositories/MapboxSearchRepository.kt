package com.moes.repositories

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A repository dedicated to handling search operations using the Mapbox Search SDK.
 * It no longer handles routing.
 */
@SuppressLint("MissingPermission")
class MapboxSearchRepository(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Retrieve the singleton instance of the SearchEngine, initialized in the Application class.
    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.GEOCODING,
        settings = SearchEngineSettings()
    )

    private val _searchSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val searchSuggestions: StateFlow<List<SearchSuggestion>> = _searchSuggestions

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val searchCallback = object : SearchSelectionCallback {
        // Called when a suggestion is selected and resolved to a full SearchResult.
        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            // Expose the single, selected result and clear the suggestions.
            _searchResults.value = listOf(result)
            _searchSuggestions.value = emptyList()
        }

        // Called when a raw text query returns multiple potential results.
        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            _searchResults.value = results
            _searchSuggestions.value = emptyList()
        }

        // Called with auto-complete style suggestions as the user types.
        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            _searchSuggestions.value = suggestions
            _searchResults.value = emptyList() // Clear previous results when new suggestions appear
        }

        override fun onError(e: Exception) {
            // Handle search errors, e.g., by logging or exposing an error state.
            e.printStackTrace()
            _searchSuggestions.value = emptyList()
            _searchResults.value = emptyList()
        }
    }

    /**
     * Called by the ViewModel when the user types a new search query.
     */
    fun onSearchQueryChanged(query: String) {
        if (query.isEmpty()) {
            clear()
            return
        }

        // As the user types, get suggestions.
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val searchOptions = SearchOptions.Builder()
                .proximity(location?.let { Point.fromLngLat(it.longitude, it.latitude) })
                .build()
            searchEngine.search(query, searchOptions, searchCallback)
        }
    }

    /**
     * Called by the ViewModel when the user selects a suggestion from the list.
     */
    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        // When a user taps a suggestion, we select it to get the full SearchResult.
        searchEngine.select(suggestion, searchCallback)
    }

    /**
     * Clears the current search results and suggestions.
     */
    fun clear() {
        _searchSuggestions.value = emptyList()
        _searchResults.value = emptyList()
    }
}
