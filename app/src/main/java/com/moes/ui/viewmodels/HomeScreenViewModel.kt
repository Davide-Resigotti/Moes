package com.moes.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.search.result.SearchResult
import com.moes.data.TrainingState
import com.moes.repositories.SearchRepository
import com.moes.repositories.TrainingRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * The ViewModel for the HomeScreen.
 * It connects to the repositories and prepares data for the UI.
 */
class HomeScreenViewModel(
    private val trainingRepository: TrainingRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    // --- State --- //

    val searchResults: StateFlow<List<SearchResult>> = searchRepository.searchResults
    val route: StateFlow<DirectionsRoute?> = searchRepository.route
    val trainingState: StateFlow<TrainingState> = trainingRepository.trainingState

    // Expose live data directly from the repository for the UI to consume.
    val liveDuration: StateFlow<Long> = trainingRepository.liveDuration
    val liveDistance: StateFlow<Double> = trainingRepository.liveDistance

    // --- Actions --- //

    fun onSearchQueryChanged(query: String) {
        searchRepository.onSearchQueryChanged(query)
    }

    fun onSearchResultSelected(result: SearchResult) {
        searchRepository.onSearchResultSelected(result)
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
}
