package com.moes.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.search.result.SearchSuggestion
import com.moes.data.TrainingState
import com.moes.data.live.LiveTrainingSession
import com.moes.repositories.MapboxNavigationRepository
import com.moes.repositories.MapboxSearchRepository
import com.moes.repositories.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The ViewModel for the HomeScreen.
 * It connects to the repositories and prepares data for the UI.
 */
@SuppressLint("MissingPermission")
class HomeScreenViewModel(
    context: Context,
    private val trainingRepository: TrainingRepository,
    private val searchRepository: MapboxSearchRepository,
    private val navigationRepository: MapboxNavigationRepository
) : ViewModel() {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // --- State --- //

    val trainingState: StateFlow<TrainingState> = trainingRepository.trainingState
    val liveTrainingSession: StateFlow<LiveTrainingSession?> =
        trainingRepository.liveTrainingSession

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchSuggestions: StateFlow<List<SearchSuggestion>> = searchRepository.searchSuggestions

    // Expose the route directly from the navigation repository.
    // The UI will observe this to draw the route and show the "Start Training" button.
    val navigationRoutes = navigationRepository.navigationRoutes

    // --- Actions --- //

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            searchRepository.clear()
        } else {
            searchRepository.onSearchQueryChanged(query)
        }
    }

    /**
     * Called when a user taps a search suggestion.
     * This function orchestrates fetching the destination's coordinates, finding the route,
     * and cleaning up the UI.
     */
    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        // Launch a coroutine to handle the selection process asynchronously.
        viewModelScope.launch {
            // First, tell the repository to select the suggestion. This will cause it
            // to emit the full search result on its `searchResults` flow.
            searchRepository.onSuggestionSelected(suggestion)

            // We collect just the first result that comes from the flow.
            val searchResult = searchRepository.searchResult.first { it != null } ?: return@launch


            // Now that we have the destination, find the user's current location.
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val origin = Point.fromLngLat(it.longitude, it.latitude)
                    // With both origin and destination, fetch the route.
                    // The UI is already observing `directionsRoute` and will update automatically.

                    navigationRepository.fetchRoute(origin, searchResult.coordinate)
                }
            }

            // Finally, clear the search bar and suggestions for a clean UI.
            clearSearch()
        }
    }

    private fun clearSearch() {
        _searchQuery.value = ""
        searchRepository.clear()
    }

    fun onStartTraining() {
        trainingRepository.startTracking()
    }

    fun onPauseTraining() {
        trainingRepository.pauseTracking()
    }

    fun onResumeTraining() {
        trainingRepository.resumeTracking()
    }

    fun onStopTraining() {
        trainingRepository.stopTracking()
    }

    /**
     * Clears the current route from the map and UI.
     */
    fun clearRoute() {
        navigationRepository.clearRoute()
    }
}
