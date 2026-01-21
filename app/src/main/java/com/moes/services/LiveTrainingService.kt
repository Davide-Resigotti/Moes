package com.moes.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.moes.R
import com.moes.data.Coordinate
import com.moes.data.TrainingState
import com.moes.data.live.LiveTrainingSegment
import com.moes.data.live.LiveTrainingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class LiveTrainingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var liveTrainingSession: LiveTrainingSession? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    if (location.accuracy > 20f) return@let

                    val lastCoord =
                        liveTrainingSession?.segments?.lastOrNull()?.coordinates?.lastOrNull()
                    if (lastCoord != null) {
                        val distance = calculateDistance(
                            lastCoord.latitude, lastCoord.longitude,
                            location.latitude, location.longitude
                        )
                        if (distance < 3.0) return@let
                    }

                    val newCoordinate = Coordinate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )
                    updateSessionWithLocation(newCoordinate)
                }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }

    private fun updateSessionWithLocation(coordinate: Coordinate) {
        val currentSession = liveTrainingSession ?: return

        val currentSegments = currentSession.segments
        if (currentSegments.isEmpty()) return

        val lastSegment = currentSegments.last()

        val updatedSegment = lastSegment.copy(
            coordinates = lastSegment.coordinates + coordinate
        )

        val updatedSegmentsList = currentSegments.dropLast(1) + updatedSegment

        liveTrainingSession = currentSession.copy(segments = updatedSegmentsList)

        updateLiveData()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_NOT_STICKY
    }

    private fun startTracking() {
        liveTrainingSession = LiveTrainingSession(
            id = UUID.randomUUID().toString(),
            startTime = System.currentTimeMillis()
        )
        updateLiveData()

        resumeTracking()
        startForegroundService()
    }

    private fun pauseTracking() {
        if (_trainingState.value == TrainingState.RUNNING) {
            _trainingState.value = TrainingState.PAUSED

            liveTrainingSession = liveTrainingSession?.let { session ->
                val lastSegment = session.segments.lastOrNull()
                if (lastSegment != null) {
                    val closedSegment = lastSegment.copy(endTime = System.currentTimeMillis())
                    val newSegments = session.segments.dropLast(1) + closedSegment
                    session.copy(segments = newSegments)
                } else {
                    session
                }
            }

            stopLocationUpdates()
            updateLiveData()
        }
    }

    private fun resumeTracking() {
        if (_trainingState.value != TrainingState.RUNNING) {
            _trainingState.value = TrainingState.RUNNING

            val newSegment = LiveTrainingSegment(startTime = System.currentTimeMillis())

            liveTrainingSession = liveTrainingSession?.let { session ->
                session.copy(segments = session.segments + newSegment)
            }

            updateLiveData()
            startLocationUpdates()
        }
    }

    private fun stopTracking() {
        if (_trainingState.value == TrainingState.RUNNING) {
            pauseTracking()
        }
        _trainingState.value = TrainingState.IDLE

        val finalSession = liveTrainingSession?.copy(
            endTime = System.currentTimeMillis()
        )

        _completedTrainingSession.value = finalSession

        liveTrainingSession = null
        _liveTrainingSession.value = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateLiveData() {
        _liveTrainingSession.value = liveTrainingSession
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000
        )
            .setMinUpdateDistanceMeters(2f)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel =
            NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
        val notification =
            NotificationCompat.Builder(this, channelId).setContentTitle("Allenamento in corso")
                .setContentText("Stiamo tracciando la tua posizione...")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        private val _trainingState = MutableStateFlow(TrainingState.IDLE)
        val trainingState: StateFlow<TrainingState> = _trainingState

        private val _liveTrainingSession = MutableStateFlow<LiveTrainingSession?>(null)
        val liveTrainingSession: StateFlow<LiveTrainingSession?> = _liveTrainingSession

        private val _completedTrainingSession = MutableStateFlow<LiveTrainingSession?>(null)
        val completedTrainingSession: StateFlow<LiveTrainingSession?> = _completedTrainingSession
    }
}