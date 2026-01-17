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

@SuppressLint("MissingPermission")
class MapboxSearchRepository(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.GEOCODING,
        settings = SearchEngineSettings()
    )

    private val _searchSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val searchSuggestions: StateFlow<List<SearchSuggestion>> = _searchSuggestions

    private val _searchResult = MutableStateFlow<SearchResult?>(null)
    val searchResult: StateFlow<SearchResult?> = _searchResult

    private val searchCallback = object : SearchSelectionCallback {
        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            _searchResult.value = result
            _searchSuggestions.value = emptyList()
        }

        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
        }

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            _searchSuggestions.value = suggestions
            _searchResult.value = null
        }

        override fun onError(e: Exception) {
            e.printStackTrace()
            _searchSuggestions.value = emptyList()
            _searchResult.value = null
        }
    }

    fun onSearchQueryChanged(query: String) {
        if (query.isEmpty()) {
            clear()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val searchOptions = SearchOptions.Builder()
                .limit(5)
                .proximity(location?.let { Point.fromLngLat(it.longitude, it.latitude) })
                .build()

            searchEngine.search(query, searchOptions, searchCallback)
        }
    }

    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        searchEngine.select(suggestion, searchCallback)
    }

    fun clear() {
        _searchSuggestions.value = emptyList()
        _searchResult.value = null
    }
}