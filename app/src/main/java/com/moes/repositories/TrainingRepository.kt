package com.moes.repositories

import android.content.Context
import android.content.Intent
import com.moes.data.Coordinate
import com.moes.data.TrainingState
import com.moes.services.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Repository that manages the lifecycle of a training session.
 * It is the single source of truth for the training state and data.
 */
class TrainingRepository(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val appContext = context.applicationContext

    // Expose the training state directly from the service.
    val trainingState: StateFlow<TrainingState> = LocationService.trainingState
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), TrainingState.IDLE)

    val isTracking: StateFlow<Boolean> = trainingState.map { it == TrainingState.TRACKING }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), false)

    val isPaused: StateFlow<Boolean> = trainingState.map { it == TrainingState.PAUSED }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), false)

    val livePath: StateFlow<List<Coordinate>> = LocationService.liveTrainingSession
        .map { it?.segments?.flatMap { it.coordinates } ?: emptyList() }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveDuration: StateFlow<Long> = LocationService.liveTrainingSession
        .map { session ->
            session?.segments?.sumOf { (it.endTime ?: System.currentTimeMillis()) - it.startTime } ?: 0L
        }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), 0L)

    val liveDistance: StateFlow<Double> = LocationService.liveTrainingSession
        .map { session ->
            session?.segments?.sumOf { segment ->
                segment.coordinates.zipWithNext { c1, c2 ->
                    calculateDistance(c1, c2)
                }.sum()
            } ?: 0.0
        }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        externalScope.launch {
            LocationService.completedTrainingSession.collect { session ->
                session?.let { databaseRepository.saveTrainingSession(it) }
            }
        }
    }

    fun startTracking() {
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        appContext.startService(intent)
    }

    fun pauseTracking() {
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_PAUSE
        }
        appContext.startService(intent)
    }

    fun resumeTracking() {
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_RESUME
        }
        appContext.startService(intent)
    }

    fun stopTracking() {
        val intent = Intent(appContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        appContext.startService(intent)
    }

    /**
     * Calculates the distance between two coordinates in meters using the Haversine formula.
     */
    private fun calculateDistance(c1: Coordinate, c2: Coordinate): Double {
        val earthRadius = 6371000 // meters
        val lat1Rad = Math.toRadians(c1.latitude)
        val lat2Rad = Math.toRadians(c2.latitude)
        val deltaLat = Math.toRadians(c2.latitude - c1.latitude)
        val deltaLon = Math.toRadians(c2.longitude - c1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}
