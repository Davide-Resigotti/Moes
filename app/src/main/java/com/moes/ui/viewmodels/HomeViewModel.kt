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

@SuppressLint("MissingPermission")
class HomeViewModel(
    context: Context,
    private val trainingRepository: TrainingRepository,
    private val searchRepository: MapboxSearchRepository,
    private val navigationRepository: MapboxNavigationRepository
) : ViewModel() {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val trainingState: StateFlow<TrainingState> = trainingRepository.trainingState
    val liveTrainingSession: StateFlow<LiveTrainingSession?> =
        trainingRepository.liveTrainingSession

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchSuggestions: StateFlow<List<SearchSuggestion>> = searchRepository.searchSuggestions

    val navigationRoutes = navigationRepository.navigationRoutes

    val finishedSessionId = trainingRepository.finishedSessionId

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            searchRepository.clear()
        } else {
            searchRepository.onSearchQueryChanged(query)
        }
    }

    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        viewModelScope.launch {
            searchRepository.onSuggestionSelected(suggestion)

            val searchResult = searchRepository.searchResult.first { it != null } ?: return@launch

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val origin = Point.fromLngLat(it.longitude, it.latitude)
                    navigationRepository.fetchRoute(origin, searchResult.coordinate)
                }
            }

            _searchQuery.value = suggestion.name
            searchRepository.clear()
        }
    }

    fun requestRouteToPoint(destination: Point) {
        viewModelScope.launch {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val origin = Point.fromLngLat(it.longitude, it.latitude)
                    navigationRepository.fetchRoute(origin, destination)
                }
            }
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
        clearRoute()
    }

    fun clearRoute() {
        navigationRepository.clearRoute()
        clearSearch()
    }

    fun clearFinishedSessionEvent() {
        trainingRepository.clearFinishedSessionEvent()
    }
}