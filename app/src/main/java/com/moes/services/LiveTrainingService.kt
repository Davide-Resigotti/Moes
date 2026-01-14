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
                    val newCoordinate = Coordinate(location.latitude, location.longitude)
                    updateSessionWithLocation(newCoordinate)
                }
            }
        }
    }

    /**
     * Logica di aggiornamento immutabile rivisitata e pulita.
     */
    private fun updateSessionWithLocation(coordinate: Coordinate) {
        val currentSession = liveTrainingSession ?: return

        // Prendiamo l'ultimo segmento attivo
        val currentSegments = currentSession.segments
        if (currentSegments.isEmpty()) return

        val lastSegment = currentSegments.last()

        // Creiamo una nuova copia del segmento con la nuova coordinata aggiunta
        val updatedSegment = lastSegment.copy(
            coordinates = lastSegment.coordinates + coordinate
        )

        // Ricostruiamo la lista dei segmenti sostituendo l'ultimo
        val updatedSegmentsList = currentSegments.dropLast(1) + updatedSegment

        // Aggiorniamo la sessione principale
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
        // Inizializza una nuova sessione
        liveTrainingSession = LiveTrainingSession(
            id = UUID.randomUUID().toString(),
            startTime = System.currentTimeMillis() // Importante: impostare l'inizio globale
        )
        updateLiveData()

        // Avvia il primo segmento e il servizio
        resumeTracking()
        startForegroundService()
    }

    private fun pauseTracking() {
        if (_trainingState.value == TrainingState.RUNNING) {
            _trainingState.value = TrainingState.PAUSED

            // Chiudi l'ultimo segmento impostando l'endTime
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
        // Riprendi solo se non sta già correndo
        if (_trainingState.value != TrainingState.RUNNING) {
            _trainingState.value = TrainingState.RUNNING

            // Aggiungi un NUOVO segmento che inizia ora
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
            pauseTracking() // Chiude correttamente l'ultimo segmento
        }
        _trainingState.value = TrainingState.IDLE

        // Emetti la sessione completa per il salvataggio
        val finalSession = liveTrainingSession?.copy(
            endTime = System.currentTimeMillis()
        )

        _completedTrainingSession.value = finalSession

        // Reset
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
        ) // Aggiornato a 3s per fluidità
            .setMinUpdateDistanceMeters(2f) // Aggiorna solo se ci si muove di 2 metri
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
                .setSmallIcon(android.R.drawable.ic_dialog_map) // Icona generica, cambiala con la tua risorsa
                .setOngoing(true) // Impedisce di cancellare la notifica swipeando
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

        // Corretto il tipo generico: Ora è LiveTrainingSession?
        private val _completedTrainingSession = MutableStateFlow<LiveTrainingSession?>(null)
        val completedTrainingSession: StateFlow<LiveTrainingSession?> = _completedTrainingSession
    }
}