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
import com.moes.data.Coordinate
import com.moes.data.LiveTrainingSegment
import com.moes.data.LiveTrainingSession
import com.moes.data.TrainingSegment
import com.moes.data.TrainingSession
import com.moes.data.TrainingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var liveTrainingSession: LiveTrainingSession? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val coordinate = Coordinate(location.latitude, location.longitude)
                    
                    // --- IMMUTABLE UPDATE --- //
                    // Create a new session object with the updated data.
                    liveTrainingSession = liveTrainingSession?.let { session ->
                        val currentSegment = session.segments.lastOrNull()
                        if (currentSegment != null) {
                            // Create a new segment with the new coordinate added.
                            val updatedSegment = currentSegment.copy(
                                coordinates = currentSegment.coordinates + coordinate
                            )
                            // Create a new list of segments with the updated one.
                            val updatedSegments = session.segments.dropLast(1) + updatedSegment
                            // Create a new session with the updated segments.
                            session.copy(segments = updatedSegments)
                        } else {
                            session // Should not happen if tracking
                        }
                    }
                    updateLiveData()
                }
            }
        }
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
        resumeTracking() // Start the first segment
        startForegroundService()
    }

    private fun pauseTracking() {
        if (_trainingState.value == TrainingState.TRACKING) {
            _trainingState.value = TrainingState.PAUSED
            // Create a new session with the last segment's endTime updated.
            liveTrainingSession = liveTrainingSession?.let { session ->
                val currentSegment = session.segments.lastOrNull()
                if (currentSegment != null) {
                    val updatedSegment = currentSegment.copy(endTime = System.currentTimeMillis())
                    val updatedSegments = session.segments.dropLast(1) + updatedSegment
                    session.copy(segments = updatedSegments)
                } else {
                    session
                }
            }
            stopLocationUpdates()
            updateLiveData()
        }
    }

    private fun resumeTracking() {
        if (_trainingState.value != TrainingState.TRACKING) {
            _trainingState.value = TrainingState.TRACKING
            // Create a new session with a new, empty segment added.
            liveTrainingSession = liveTrainingSession?.copy(
                segments = liveTrainingSession!!.segments + LiveTrainingSegment(startTime = System.currentTimeMillis())
            )
            startLocationUpdates()
        }
    }

    private fun stopTracking() {
        if (_trainingState.value == TrainingState.TRACKING) {
            pauseTracking()
        }
        _trainingState.value = TrainingState.IDLE
        liveTrainingSession?.let { session ->
            val finalSession = session.copy(endTime = System.currentTimeMillis())
            _completedTrainingSession.value = finalSession.toTrainingSession()
        }
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
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Workout in Progress")
            .setContentText("Tracking your location...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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

        private val _completedTrainingSession = MutableStateFlow<TrainingSession?>(null)
        val completedTrainingSession: StateFlow<TrainingSession?> = _completedTrainingSession
    }
}

private fun LiveTrainingSession.toTrainingSession(): TrainingSession {
    val finalSegments = this.segments.map { liveSegment ->
        TrainingSegment(
            startTime = liveSegment.startTime,
            endTime = liveSegment.endTime ?: System.currentTimeMillis(),
            coordinates = liveSegment.coordinates
        )
    }
    return TrainingSession(
        id = this.id,
        startTime = this.startTime,
        endTime = this.endTime ?: System.currentTimeMillis(),
        segments = finalSegments
    )
}
